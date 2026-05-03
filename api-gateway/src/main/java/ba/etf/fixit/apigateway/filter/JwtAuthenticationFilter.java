package ba.etf.fixit.apigateway.filter;

import ba.etf.fixit.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Globalni filter koji se izvrsava za SVAKI zahtjev kroz gateway.
 *
 * Logika:
 * 1. Javni putevi — propusti, ali dodaj X-Gateway-Secret
 * 2. Zasticeni putevi — zahtijevaju valjan Bearer access token
 * 3. Iz tokena ekstraktuj korisnicke podatke, proslijedi ih kao X-Korisnik-* zaglavlja
 * 4. Uvijek dodaj X-Gateway-Secret — mikroservisi ga provjeravaju da
 *    zahtjev dolazi kroz gateway a ne direktno
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    @Value("${gateway.internal-secret:local-dev-secret}")
    private String gatewaySecret;

    private static final List<String> JAVNI_PUTEVI = List.of(
            "/api/auth/registracija",
            "/api/auth/prijava",
            "/api/auth/refresh",
            "/api/auth/odjava",
            "/actuator/health",
            "/actuator/info"
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest zahtjev = exchange.getRequest();
        String putanja = zahtjev.getPath().value();

        if (jeJavniPut(putanja)) {
            log.debug("Javni put, preskacanje autentifikacije: {}", putanja);
            ServerHttpRequest saSecretom = zahtjev.mutate()
                    .header("X-Gateway-Secret", gatewaySecret)
                    .build();
            return chain.filter(exchange.mutate().request(saSecretom).build());
        }

        String authZaglavlje = zahtjev.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authZaglavlje == null || !authZaglavlje.startsWith("Bearer ")) {
            log.warn("Zahtjev bez Bearer tokena za put: {}", putanja);
            return odbiZahtjev(exchange, HttpStatus.UNAUTHORIZED,
                    "Potrebna autentifikacija. Dodajte Bearer token.");
        }

        String token = authZaglavlje.substring(7);

        try {
            if (!jwtUtil.jeValjanAccessToken(token)) {
                return odbiZahtjev(exchange, HttpStatus.UNAUTHORIZED,
                        "Token je istekao ili nije valjan. Koristite /api/auth/refresh za osvjezavanje.");
            }

            Claims claims = jwtUtil.parsirajToken(token);
            String email = claims.getSubject();
            String uloga = claims.get("uloga", String.class);
            Long korisnikId = claims.get("korisnikId", Long.class);

            log.debug("Autentificiran korisnik: {} [{}] za put: {}", email, uloga, putanja);

            ServerHttpRequest izmijenjeniZahtjev = zahtjev.mutate()
                    .header("X-Korisnik-Email", email)
                    .header("X-Korisnik-Uloga", uloga != null ? uloga : "")
                    .header("X-Korisnik-Id", korisnikId != null ? korisnikId.toString() : "")
                    .header("X-Gateway-Secret", gatewaySecret)
                    .build();

            return chain.filter(exchange.mutate().request(izmijenjeniZahtjev).build());

        } catch (JwtException e) {
            log.warn("Nevaljan JWT token: {}", e.getMessage());
            return odbiZahtjev(exchange, HttpStatus.UNAUTHORIZED,
                    "Nevaljan token: " + e.getMessage());
        } catch (Exception e) {
            log.error("Greska pri obradi tokena", e);
            return odbiZahtjev(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Interna greska pri autentifikaciji.");
        }
    }

    private boolean jeJavniPut(String putanja) {
        return JAVNI_PUTEVI.stream().anyMatch(putanja::startsWith);
    }

    private Mono<Void> odbiZahtjev(ServerWebExchange exchange, HttpStatus status, String poruka) {
        ServerHttpResponse odgovor = exchange.getResponse();
        odgovor.setStatusCode(status);
        odgovor.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String tijelo = String.format("{\"greska\":\"%s\",\"poruka\":\"%s\"}",
                status.name(), poruka);
        DataBuffer buffer = odgovor.bufferFactory()
                .wrap(tijelo.getBytes(StandardCharsets.UTF_8));
        return odgovor.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}