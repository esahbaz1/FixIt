package ba.etf.fixit.reportservice.dto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KomentarRequestDTO {
    @NotNull(message = "Korisnik ID je obavezan") 
    private Long korisnikId;
    private String naslov;
    @NotBlank(message = "Tekst ne smije biti prazan") 
    private String tekst;
    private Boolean interan = false;
}
