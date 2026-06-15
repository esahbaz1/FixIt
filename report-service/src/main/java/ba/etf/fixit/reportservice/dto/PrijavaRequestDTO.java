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
    @Size(max = 200, message = "Naslov može imati najviše 200 karaktera")
    private String naslov;

    @NotBlank(message = "Opis ne smije biti prazan")
    private String opis;

    @NotNull(message = "Latitude je obavezna")
    @Min(value = -90, message = "Nevalidna latitude vrijednost (mora biti između -90 i 90)")
    @Max(value = 90, message = "Nevalidna latitude vrijednost (mora biti između -90 i 90)")
    private Double latitude;

    @NotNull(message = "Longitude je obavezna")
    @Min(value = -180, message = "Nevalidna longitude vrijednost (mora biti između -180 i 180)")
    @Max(value = 180, message = "Nevalidna longitude vrijednost (mora biti između -180 i 180)")
    private Double longitude;

    @Size(max = 300, message = "Adresa može imati najviše 300 karaktera")
    private String adresa;

    @NotNull(message = "Kategorija ID je obavezna")
    private Long kategorijaId;

    @NotNull(message = "Prioritet je obavezan")
    private PrioritetPrijave prioritet;

    private LocalDateTime datumRoka;
}