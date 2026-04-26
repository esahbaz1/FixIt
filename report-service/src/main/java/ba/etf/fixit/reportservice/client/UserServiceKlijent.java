package ba.etf.fixit.reportservice.client;

import ba.etf.fixit.reportservice.dto.KorisnikDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceKlijent {

    private static final Logger log = LoggerFactory.getLogger(UserServiceKlijent.class);

    private static final String USER_SERVICE_URL = "http://user-service";

    private final RestTemplate restTemplate;

    public UserServiceKlijent(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public KorisnikDTO validirajKorisnika(Long korisnikId) {
        String url = USER_SERVICE_URL + "/api/korisnici/" + korisnikId;
        try {
            log.info("Sinhroni poziv user-service: GET {}", url);
            KorisnikDTO korisnik = restTemplate.getForObject(url, KorisnikDTO.class);

            if (korisnik == null) {
                log.warn("user-service vratio null za korisnikId={}", korisnikId);
                return null;
            }

            if (Boolean.FALSE.equals(korisnik.getAktivan())) {
                log.warn("Korisnik ID={} je neaktivan, prijava odbijena.", korisnikId);
                throw new KorisnikNijeAktivanException(
                    "Korisnik sa ID-em " + korisnikId + " je deaktiviran i ne može kreirati prijave."
                );
            }

            log.info("Korisnik ID={} ({} {}) validiran uspješno.", korisnikId,
                korisnik.getIme(), korisnik.getPrezime());
            return korisnik;

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("user-service: korisnik ID={} nije pronađen (404).", korisnikId);
            throw new KorisnikNijePronadjenException(
                "Korisnik sa ID-em " + korisnikId + " nije pronađen u sistemu."
            );

        } catch (ResourceAccessException e) {
        
            log.error("user-service nije dostupan ({}). Prijava se kreira bez validacije korisnika.", e.getMessage());
            return null;

        } catch (KorisnikNijeAktivanException | KorisnikNijePronadjenException e) {
            throw e; 

        } catch (Exception e) {
            log.error("Neočekivana greška pri pozivu user-service: {}", e.getMessage());
            return null; 
        }
    }

    public static class KorisnikNijePronadjenException extends RuntimeException {
        public KorisnikNijePronadjenException(String message) { super(message); }
    }

    public static class KorisnikNijeAktivanException extends RuntimeException {
        public KorisnikNijeAktivanException(String message) { super(message); }
    }
}
