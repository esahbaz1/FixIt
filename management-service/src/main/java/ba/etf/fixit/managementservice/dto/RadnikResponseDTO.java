package ba.etf.fixit.managementservice.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RadnikResponseDTO {
    private Long id; 
    private Long korisnikId; 
    private Long gradskaSluzbaId;
    private String nazivSluzbe; 
    private String pozicija; 
    private String kompetencije; 
    private Boolean aktivan;
    private String ime;      
    private String prezime;
}
