package ba.etf.fixit.managementservice.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "radnik")
public class Radnik {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "korisnik_id", nullable = false)
    private Long korisnikId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gradska_sluzba_id", nullable = false)
    private GradskaSluzba gradskaSluzba;

    @Column(length = 100)
    private String pozicija;

    @Column(length = 300)
    private String kompetencije;

    @Column(nullable = false)
    private Boolean aktivan = true;
}
