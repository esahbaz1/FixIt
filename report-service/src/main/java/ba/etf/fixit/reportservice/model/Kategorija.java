package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "kategorija")
public class Kategorija {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Column(nullable = false, unique = true, length = 100)
    private String naziv;
    @Column(length = 300)
    private String opis;
    @Column(name = "gradska_sluzba_id")
    private Long gradskaSluzbaId;
}
