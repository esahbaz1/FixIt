package ba.etf.fixit.managementservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Klijent za komunikaciju sa report-service.
 * Koristi se za dodjelu radnika (odgovornog lica) na prijavu.
 *
 * VAŽNO: Svaki poziv mora uključiti X-Gateway-Secret header jer
 * report-service odbija direktne pozive bez njega (403 FORBIDDEN).
 */
@Component
public class ReportServiceKlijent {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceKlijent.class);

    private final RestTemplate restTemplate;

    @Value("${gateway.internal-secret:local-dev-secret}")
    private String gatewaySecret;

    @Value("${discovery.report-service.direct-base-url:http://report-service:8083}")
    private String reportServiceBaseUrl;

    public ReportServiceKlijent(@Qualifier("directRestTemplate") RestTemplate restTemplate) {
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
     * Dodjeljuje radnika (po korisnikId) kao odgovorno lice na prijavu u report-service.
     *
     * @param prijavaId  ID prijave u report-service
     * @param korisnikId korisnikId radnika koji se dodjeljuje
     */
    public void dodijeliRadnikaNaPrijavu(Long prijavaId, Long korisnikId) {
        String url = reportServiceBaseUrl + "/api/prijave/" + prijavaId
                + "/dodjeli-radnika?korisnikId=" + korisnikId;
        try {
            log.info("Dodjela radnika korisnikId={} na prijavu ID={} -> {}", korisnikId, prijavaId, url);
            restTemplate.exchange(url, HttpMethod.PATCH, headers(), String.class);
            log.info("Radnik uspjesno dodijeljen prijavi ID={}", prijavaId);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("report-service: prijava ID={} nije pronadjena (404)", prijavaId);
            throw new PrijavaNotFoundException("Prijava sa ID-em " + prijavaId + " nije pronadjena u sistemu.");
        } catch (HttpServerErrorException e) {
            log.error("report-service vratio gresku {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ReportServiceNedostupanException("Report servis je vratio grešku: " + e.getStatusCode());
        } catch (ResourceAccessException e) {
            log.error("report-service nije dostupan na {}: {}", reportServiceBaseUrl, e.getMessage());
            throw new ReportServiceNedostupanException("Report servis je trenutno nedostupan. Pokusajte ponovo.");
        } catch (Exception e) {
            log.error("Greska pri dodjeli radnika na prijavu: {}", e.getMessage());
            throw new RuntimeException("Neocekivana greska pri dodjeli radnika: " + e.getMessage());
        }
    }

    public static class PrijavaNotFoundException extends RuntimeException {
        public PrijavaNotFoundException(String message) { super(message); }
    }

    public static class ReportServiceNedostupanException extends RuntimeException {
        public ReportServiceNedostupanException(String message) { super(message); }
    }
}
