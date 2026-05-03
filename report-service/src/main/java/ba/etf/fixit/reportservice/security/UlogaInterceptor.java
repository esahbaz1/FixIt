package ba.etf.fixit.reportservice.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;

/**
 * Kontrola pristupa za report-service.
 *
 * Pravila:
 *  - GET /api/prijave/** — svi autentificirani (GRADJANIN moze vidjeti prijave)
 *  - POST /api/prijave  — GRADJANIN, RADNIK, RUKOVODILAC, ADMIN (kreiranje prijave)
 *  - PUT/PATCH /api/prijave/** — RADNIK, RUKOVODILAC, ADMIN (izmjena statusa)
 *  - DELETE /api/prijave/** — RUKOVODILAC, ADMIN
 *  - POST /api/prijave/{id}/komentari — svi autentificirani
 */
@Component
public class UlogaInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        String uloga = KorisnikKontekst.uloga();
        String metoda = request.getMethod();
        String putanja = request.getRequestURI();

        if (putanja.startsWith("/actuator") || putanja.startsWith("/swagger-ui")
                || putanja.startsWith("/api-docs")) {
            return true;
        }

        if (uloga == null || uloga.isBlank()) {
            pisGresku(response, HttpStatus.UNAUTHORIZED, "Korisnik nije autentificiran");
            return false;
        }

        // Brisanje prijave — samo RUKOVODILAC i ADMIN
        if ("DELETE".equals(metoda) && putanja.matches("/api/prijave/\\d+")) {
            if (!imaUlogu(uloga, "RUKOVODILAC", "ADMIN")) {
                pisGresku(response, HttpStatus.FORBIDDEN, "Brisanje prijava dozvoljeno samo RUKOVODILAC/ADMIN");
                return false;
            }
        }

        // Izmjena statusa prijave — RADNIK, RUKOVODILAC, ADMIN
        if (("PUT".equals(metoda) || "PATCH".equals(metoda)) && putanja.matches("/api/prijave/\\d+.*")) {
            if (!imaUlogu(uloga, "RADNIK", "RUKOVODILAC", "ADMIN")) {
                pisGresku(response, HttpStatus.FORBIDDEN,
                        "Izmjena prijava dozvoljena samo za RADNIK/RUKOVODILAC/ADMIN");
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
