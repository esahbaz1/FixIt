package ba.etf.fixit.reportservice.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FotografijaResponseDTO {
    private Long id;
    private Long prijavaId;
    private String putanja;
    private LocalDateTime datumUnosa;
}
