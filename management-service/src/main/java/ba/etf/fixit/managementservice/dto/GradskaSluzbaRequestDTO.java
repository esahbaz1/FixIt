package ba.etf.fixit.managementservice.dto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradskaSluzbaRequestDTO {
    @NotBlank(message = "Naziv sluzbe ne smije biti prazan")
    private String naziv;
    private String opis;
    @Email(message = "Kontakt email mora biti validan")
    private String kontaktEmail;
    private String kontaktTelefon;
}
