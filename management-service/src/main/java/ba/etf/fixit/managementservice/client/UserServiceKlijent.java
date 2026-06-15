package ba.etf.fixit.managementservice.client;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Klijent za komunikaciju sa user-service.
 * Koristi se za dohvat korisničkih podataka (ime, prezime) po korisnikId.
 *
 * VAŽNO: Svaki poziv mora uključiti X-Gateway-Secret header jer
 * user-service odbija direktne pozive bez njega (403 FORBIDDEN).
 */
@Component
public class UserServiceKlijent {

    private static final Logger log = LoggerFactory.getLogger(UserServiceKlijent.class);
    private static final String USER_SERVICE_URL = "http://user-service";

    private final RestTemplate restTemplate;

    @Value("${gateway.internal-secret:local-dev-secret}")
    private String gatewaySecret;

    public UserServiceKlijent(@Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** Kreira HttpEntity sa obaveznim internim headerima. */
    private HttpEntity<Void> headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("X-Gateway-Secret", gatewaySecret);
        h.set("X-Korisnik-Email", "system@fixit.internal");
        h.set("X-Korisnik-Uloga", "ADMIN");
        h.set("X-Korisnik-Id", "0");
        return new HttpEntity<>(h);
    }

    /**
     * Dohvaca ime i prezime korisnika po ID-u.
     * U slučaju greške vraća null (ne bacamo iznimku - dodjela radnika ne smije puci zbog nedostupnog user-service).
     */
    public KorisnikInfo dohvatiKorisnika(Long korisnikId) {
        if (korisnikId == null) return null;
        try {
            String url = USER_SERVICE_URL + "/api/korisnici/" + korisnikId;
            ResponseEntity<KorisnikInfo> resp = restTemplate.exchange(
                    url, HttpMethod.GET, headers(), KorisnikInfo.class);
            return resp.getBody();
        } catch (Exception e) {
            log.warn("Nije moguće dohvatiti korisnika ID={} iz user-service: {}", korisnikId, e.getMessage());
            return null;
        }
    }

    @Data
    public static class KorisnikInfo {
        private Long id;
        private String ime;
        private String prezime;
        private String email;
    }
}