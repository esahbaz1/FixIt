package ba.etf.fixit.reportservice.dto;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KomentarResponseDTO {
    private Long id; 
    private Long prijavaId; 
    private Long korisnikId;
    private String naslov; 
    private String tekst; 
    private Boolean interan; 
    private LocalDateTime datumKreiranja;
}
