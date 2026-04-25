package ba.etf.fixit.userservice.dto;
import ba.etf.fixit.userservice.model.UlogaKorisnika;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO odgovor - NIKAD ne vracamo lozinku! */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KorisnikResponseDTO {
    private Long id;
    private String ime;
    private String prezime;
    private String email;
    private UlogaKorisnika uloga;
    private Boolean aktivan;
    private LocalDateTime datumKreiranja;
}
