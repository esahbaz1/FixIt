package ba.etf.fixit.reportservice.client;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Klijent za komunikaciju sa management-service.
 * Koristi se za dohvat naziva gradske službe i imena odgovornog radnika.
 *
 * VAŽNO: Svaki poziv mora uključiti X-Gateway-Secret header jer
 * management-service odbija direktne pozive bez njega (403 FORBIDDEN).
 */
@Component
public class ManagementServiceKlijent {

    private static final Logger log = LoggerFactory.getLogger(ManagementServiceKlijent.class);

    private final RestTemplate restTemplate;

    @Value("${gateway.internal-secret:local-dev-secret}")
    private String gatewaySecret;

    @Value("${discovery.management-service.direct-base-url:http://management-service:8082}")
    private String managementServiceBaseUrl;

    public ManagementServiceKlijent(@Qualifier("directRestTemplate") RestTemplate restTemplate) {
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
     * Dohvata naziv gradske službe po ID-u.
     * Vraća null ako služba nije pronađena ili management-service nije dostupan.
     */
    public String dohvatiNazivSluzbe(Long sluzbaId) {
        if (sluzbaId == null) return null;
        try {
            String url = managementServiceBaseUrl + "/api/gradske-sluzbe/" + sluzbaId;
            ResponseEntity<GradskaSluzbaInfo> resp = restTemplate.exchange(
                    url, HttpMethod.GET, headers(), GradskaSluzbaInfo.class);
            GradskaSluzbaInfo info = resp.getBody();
            return info != null ? info.getNaziv() : null;
        } catch (Exception e) {
            log.warn("Nije moguće dohvatiti gradsku službu ID={}: {}", sluzbaId, e.getMessage());
            return null;
        }
    }

    /**
     * Dohvata puno ime odgovornog radnika po korisnikId.
     * Vraća null ako nije pronađen ili management-service nije dostupan.
     */
    public String dohvatiImeRadnika(Long korisnikId) {
        if (korisnikId == null) return null;
        try {
            String url = managementServiceBaseUrl + "/api/radnici/korisnik/" + korisnikId;
            ResponseEntity<RadnikInfo> resp = restTemplate.exchange(
                    url, HttpMethod.GET, headers(), RadnikInfo.class);
            RadnikInfo info = resp.getBody();
            if (info != null && info.getIme() != null) {
                return (info.getIme() + " " + (info.getPrezime() != null ? info.getPrezime() : "")).trim();
            }
            return null;
        } catch (Exception e) {
            log.warn("Nije moguće dohvatiti radnika za korisnikId={}: {}", korisnikId, e.getMessage());
            return null;
        }
    }

    @Data
    public static class GradskaSluzbaInfo {
        private Long id;
        private String naziv;
        private String opis;
    }

    @Data
    public static class RadnikInfo {
        private Long id;
        private Long korisnikId;
        private String ime;
        private String prezime;
        private String nazivSluzbe;
    }
}
