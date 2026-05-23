package ba.etf.fixit.reportservice.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidacijaResponseDTO {
    private Long id;
    private Long prijavaId;
    private Long korisnikId;
    private Boolean potvrdjeno;
    private LocalDateTime datumValidacije;
}
