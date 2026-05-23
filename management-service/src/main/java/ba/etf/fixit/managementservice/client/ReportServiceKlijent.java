package ba.etf.fixit.managementservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Klijent za komunikaciju sa report-service.
 * Koristi se za dodjelu radnika (odgovornog lica) na prijavu.
 */
@Component
public class ReportServiceKlijent {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceKlijent.class);
    private static final String REPORT_SERVICE_URL = "http://report-service";

    private final RestTemplate restTemplate;

    public ReportServiceKlijent(@Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Dodjeljuje radnika (po korisnikId) kao odgovorno lice na prijavu u report-service.
     *
     * @param prijavaId  ID prijave u report-service
     * @param korisnikId korisnikId radnika koji se dodjeljuje
     */
    public void dodijeliRadnikaNaPrijavu(Long prijavaId, Long korisnikId) {
        String url = REPORT_SERVICE_URL + "/api/prijave/" + prijavaId + "/dodjeli-radnika?korisnikId=" + korisnikId;
        try {
            log.info("Dodjela radnika korisnikId={} na prijavu ID={}", korisnikId, prijavaId);
            restTemplate.patchForObject(url, null, Void.class);
            log.info("Radnik uspjesno dodijeljen prijavi ID={}", prijavaId);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("report-service: prijava ID={} nije pronadjena (404)", prijavaId);
            throw new PrijavaNotFoundException("Prijava sa ID-em " + prijavaId + " nije pronadjena u sistemu.");
        } catch (ResourceAccessException e) {
            log.error("report-service nije dostupan: {}", e.getMessage());
            throw new ReportServiceNedostupanException("Report servis je trenutno nedostupan. Pokusajte ponovo.");
        } catch (Exception e) {
            log.error("Greska pri dodjeli radnika na prijavu: {}", e.getMessage());
            throw new RuntimeException("Neocekivana greska pri dodjeli radnika.");
        }
    }

    public static class PrijavaNotFoundException extends RuntimeException {
        public PrijavaNotFoundException(String message) { super(message); }
    }

    public static class ReportServiceNedostupanException extends RuntimeException {
        public ReportServiceNedostupanException(String message) { super(message); }
    }
}