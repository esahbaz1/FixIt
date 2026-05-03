package ba.etf.fixit.reportservice.security;  

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class GatewaySecurityFilter extends OncePerRequestFilter {

    @Value("${gateway.internal-secret:local-dev-secret}")
    private String gatewaySecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String putanja = request.getRequestURI();

        if (putanja.startsWith("/actuator") || putanja.startsWith("/swagger-ui")
                || putanja.startsWith("/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String primljeniSecret = request.getHeader("X-Gateway-Secret");
        if (!gatewaySecret.equals(primljeniSecret)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"greska\":\"FORBIDDEN\"," +
                    "\"poruka\":\"Direktan pristup mikroservisu nije dozvoljen. Koristite API Gateway.\"}");
            return;
        }

        // Procitaj korisnicke podatke koje je gateway proslijedio
        String idStr = request.getHeader("X-Korisnik-Id");
        String email = request.getHeader("X-Korisnik-Email");
        String uloga = request.getHeader("X-Korisnik-Uloga");

        if (email != null && !email.isBlank()) {
            Long korisnikId = null;
            if (idStr != null && !idStr.isBlank()) {
                try { korisnikId = Long.parseLong(idStr); }
                catch (NumberFormatException ignored) {}
            }
            KorisnikKontekst.postavi(
                    new KorisnikKontekst.KorisnikPodaci(korisnikId, email, uloga));
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            KorisnikKontekst.obrisi();
        }
    }
}