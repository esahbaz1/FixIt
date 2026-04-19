package ba.etf.fixit.userservice.dto;
import jakarta.validation.constraints.*;
import java.util.List;

public class KorisnikBatchRequestDTO {
    private List<RegistracijaRequestDTO> korisnici;

    public List<RegistracijaRequestDTO> getKorisnici() {
        return korisnici;
    }

    public void setKorisnici(List<RegistracijaRequestDTO> korisnici) {
        this.korisnici = korisnici;
    }
}
