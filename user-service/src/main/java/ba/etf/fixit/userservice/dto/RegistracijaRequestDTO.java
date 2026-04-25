package ba.etf.fixit.userservice.dto;
import ba.etf.fixit.userservice.model.UlogaKorisnika;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistracijaRequestDTO {
    @NotBlank(message = "Ime ne smije biti prazno")
    private String ime;
    @NotBlank(message = "Prezime ne smije biti prazno")
    private String prezime;
    @Email(message = "Email mora biti u ispravnom formatu")
    @NotBlank(message = "Email ne smije biti prazan")
    private String email;
    @NotBlank(message = "Lozinka ne smije biti prazna")
    @Size(min = 8, message = "Lozinka mora imati najmanje 8 karaktera")
    private String lozinka;
    private UlogaKorisnika uloga;
}
