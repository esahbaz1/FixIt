package ba.etf.fixit.managementservice.dto;
import ba.etf.fixit.managementservice.model.UlogaKorisnika;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KorisnikProfilRequestDTO {
    @NotNull(message = "Korisnik ID je obavezan")
    private Long korisnikId;
    private String telefon;
    private String adresa;
    private UlogaKorisnika uloga;
}
