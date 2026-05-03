package ba.etf.fixit.notificationservice.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;

/**
 * Kontrola pristupa za notification-service.
 *
 * Pravila:
 *  - GET /api/notifikacije/** — korisnik moze citati SVOJE notifikacije
 *    (provjera u servisu: korisnikId iz konteksta == notifikacija.korisnikId)
 *  - POST /api/notifikacije — interno slanje (obicno pozivaju drugi mikroservisi)
 *    Dozvoljavamo svim autentificiranim korisnicima i internim zahtjevima
 *  - DELETE — samo ADMIN
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

        // Interni pozivi od strane drugih mikroservisa (X-Interni-Servis zaglavlje)
        // Ovo zaglavlje dodaje gateway ili drugi mikroservis — ne klijent
        String interniServis = request.getHeader("X-Interni-Servis");
        if (interniServis != null && !interniServis.isBlank()) {
            return true;
        }

        if (uloga == null || uloga.isBlank()) {
            pisGresku(response, HttpStatus.UNAUTHORIZED, "Korisnik nije autentificiran");
            return false;
        }

        // Brisanje — samo ADMIN
        if ("DELETE".equals(metoda)) {
            if (!imaUlogu(uloga, "ADMIN")) {
                pisGresku(response, HttpStatus.FORBIDDEN, "Brisanje notifikacija dozvoljeno samo ADMIN");
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
