package ba.etf.fixit.reportservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidacijaRequestDTO {
    @NotNull(message = "Korisnik ID je obavezan")
    private Long korisnikId;

    @NotNull(message = "Vrijednost potvrdjeno je obavezna")
    private Boolean potvrdjeno;
}
