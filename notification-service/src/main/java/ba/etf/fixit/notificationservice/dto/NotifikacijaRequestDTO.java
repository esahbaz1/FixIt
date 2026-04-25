package ba.etf.fixit.notificationservice.dto;
import ba.etf.fixit.notificationservice.model.TipNotifikacije;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifikacijaRequestDTO {
    @NotNull(message = "Korisnik ID je obavezan") 
    private Long korisnikId;
    private Long prijavaId;
    @NotBlank(message = "Naslov ne smije biti prazan") 
    private String naslov;
    @NotBlank(message = "Tekst ne smije biti prazan") 
    private String tekst;
    @NotNull(message = "Tip notifikacije je obavezan") 
    private TipNotifikacije tip;
}
