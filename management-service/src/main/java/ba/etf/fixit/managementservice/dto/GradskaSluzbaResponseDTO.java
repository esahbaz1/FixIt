package ba.etf.fixit.managementservice.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradskaSluzbaResponseDTO {
    private Long id; 
    private String naziv; 
    private String opis;
    private String kontaktEmail; 
    private String kontaktTelefon; 
    private Boolean aktivan;
}
