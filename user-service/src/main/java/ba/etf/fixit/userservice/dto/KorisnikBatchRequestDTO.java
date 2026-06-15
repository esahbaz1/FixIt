package ba.etf.fixit.userservice.dto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KorisnikBatchRequestDTO {
    private List<RegistracijaRequestDTO> korisnici;
}
