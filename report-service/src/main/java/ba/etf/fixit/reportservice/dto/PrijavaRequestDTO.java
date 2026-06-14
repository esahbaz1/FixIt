package ba.etf.fixit.reportservice.dto;

import ba.etf.fixit.reportservice.model.PrioritetPrijave;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrijavaRequestDTO {
    @NotBlank(message = "Naslov ne smije biti prazan")
    private String naslov;
    @NotBlank(message = "Opis ne smije biti prazan")
    private String opis;
    @NotNull(message = "Latitude je obavezna")
    private Double latitude;
    @NotNull(message = "Longitude je obavezna")
    private Double longitude;
    private String adresa;
    @NotNull(message = "Kategorija ID je obavezna")
    private Long kategorijaId;
    private PrioritetPrijave prioritet;
    private LocalDateTime datumRoka;
}
