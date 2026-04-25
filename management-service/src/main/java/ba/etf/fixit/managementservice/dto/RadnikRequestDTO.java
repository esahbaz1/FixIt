package ba.etf.fixit.managementservice.dto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RadnikRequestDTO {
    @NotNull(message = "Korisnik ID je obavezan")
    private Long korisnikId;
    @NotNull(message = "Gradska sluzba ID je obavezna")
    private Long gradskaSluzbaId;
    private String pozicija;
    private String kompetencije;
}
