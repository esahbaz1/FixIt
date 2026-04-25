package ba.etf.fixit.userservice.dto;
import ba.etf.fixit.userservice.model.UlogaKorisnika;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private Long id;
    private String email;
    private String ime;
    private String prezime;
    private UlogaKorisnika uloga;
    private String poruka;
}
