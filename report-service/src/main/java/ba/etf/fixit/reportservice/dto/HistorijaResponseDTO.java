package ba.etf.fixit.reportservice.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorijaResponseDTO {
    private Long id;
    private Long prijavaId;
    private String statusIz;
    private String statusU;
    private Long korisnikId;
    private LocalDateTime datumPromjene;
}
