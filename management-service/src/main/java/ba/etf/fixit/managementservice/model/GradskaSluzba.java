package ba.etf.fixit.managementservice.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gradska_sluzba")
public class GradskaSluzba {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Naziv sluzbe ne smije biti prazan")
    @Column(nullable = false, unique = true, length = 150)
    private String naziv;
    @Column(length = 300)
    private String opis;
    @Column(name = "kontakt_email", length = 150)
    private String kontaktEmail;
    @Column(name = "kontakt_telefon", length = 20)
    private String kontaktTelefon;
    @Column(nullable = false)
    private Boolean aktivan = true;
}
