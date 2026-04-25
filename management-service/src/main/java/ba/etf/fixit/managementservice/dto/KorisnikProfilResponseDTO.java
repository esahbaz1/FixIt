package ba.etf.fixit.managementservice.dto;
import ba.etf.fixit.managementservice.model.UlogaKorisnika;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KorisnikProfilResponseDTO {
    private Long id; 
    private Long korisnikId; 
    private String telefon;
    private String adresa; 
    private UlogaKorisnika uloga; 
    private Boolean aktivan;
}
