package ba.etf.fixit.managementservice.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * Interceptor za provjeru uloga korisnika na nivou managementservisa.
 *
 * Koristi se u kombinaciji sa GatewaySecurityFilter — filtar vec postavio
 * korisnicke podatke u ThreadLocal, a ovaj interceptor provjerava
 * ima li korisnik potrebnu ulogu za pristup resursu.
 *
 * Pravila pristupa za management-service:
 *  - GET /api/radnici/** — RADNIK, RUKOVODILAC, ADMIN
 *  - POST/PUT/DELETE /api/radnici/** — RUKOVODILAC, ADMIN
 *  - GET /api/sluzbe/** — svi autentificirani
 *  - POST/PUT/DELETE /api/sluzbe/** — ADMIN
 */
@Component
public class UlogaInterceptor implements HandlerInterceptor {

    private static final Set<String> ADMIN_METODE = Set.of("POST", "PUT", "DELETE", "PATCH");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        String uloga = KorisnikKontekst.uloga();
        String metoda = request.getMethod();
        String putanja = request.getRequestURI();

        // Actuator putevi — uvijek dozvoljeni
        if (putanja.startsWith("/actuator")) {
            return true;
        }

        // Swagger — dozvoljeno bez auth (samo u dev modu)
        if (putanja.startsWith("/swagger-ui") || putanja.startsWith("/api-docs")) {
            return true;
        }

        // Ako nema uloge — korisnik nije autentificiran (zahtjev nije dosao kroz gateway)
        if (uloga == null || uloga.isBlank()) {
            pisGresku(response, HttpStatus.UNAUTHORIZED, "Korisnik nije autentificiran");
            return false;
        }

        // Provjera za radnike — izmjene zahtijevaju RUKOVODILAC ili ADMIN
        if (putanja.startsWith("/api/radnici") && ADMIN_METODE.contains(metoda)) {
            if (!imaUlogu(uloga, "RUKOVODILAC", "ADMIN")) {
                pisGresku(response, HttpStatus.FORBIDDEN,
                        "Nemate dozvolu za ovu operaciju. Potrebna uloga: RUKOVODILAC ili ADMIN");
                return false;
            }
        }

        // Provjera za sluzbe — sve izmjene zahtijevaju ADMIN
        if (putanja.startsWith("/api/sluzbe") && ADMIN_METODE.contains(metoda)) {
            if (!imaUlogu(uloga, "ADMIN")) {
                pisGresku(response, HttpStatus.FORBIDDEN,
                        "Nemate dozvolu za ovu operaciju. Potrebna uloga: ADMIN");
                return false;
            }
        }

        return true;
    }

    private boolean imaUlogu(String korisnikUloga, String... dozvoljeneUloge) {
        return Arrays.asList(dozvoljeneUloge).contains(korisnikUloga);
    }

    private void pisGresku(HttpServletResponse response, HttpStatus status, String poruka)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                String.format("{\"greska\":\"%s\",\"poruka\":\"%s\"}", status.name(), poruka)
        );
    }
}
