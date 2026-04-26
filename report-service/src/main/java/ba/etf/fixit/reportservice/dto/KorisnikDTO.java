package ba.etf.fixit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KorisnikDTO {
    private Long id;
    private String ime;
    private String prezime;
    private String email;
    private String uloga;
    private Boolean aktivan;
}
