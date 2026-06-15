package ba.etf.fixit.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SIGURNOSNA NAPOMENA: Uloga je namjerno uklonjena.
 * Svaki novi korisnik automatski dobija ulogu GRADJANIN.
 * Dodjela vise uloge vrsi se kroz PUT /api/korisnici/{id}/uloga (samo ADMIN).
 */
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
    @Pattern(
        regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$",
        message = "Email mora sadrzavati valjanu domenu (npr. korisnik@example.com)"
    )
    private String email;

    
    @NotBlank(message = "Lozinka ne smije biti prazna")
    @Size(min = 8, message = "Lozinka mora imati najmanje 8 karaktera")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$",
        message = "Lozinka mora sadrzavati najmanje jedno veliko slovo, jedan broj i jedan specijalni karakter"
    )
    private String lozinka;
}
