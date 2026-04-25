package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "historija_prijave")
public class HistorijaPrijave {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tip_pr_id", nullable = false)
    private TipPromjene tipPromjene;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prijava_id", nullable = false)
    private Prijava prijava;

    @Column(name = "korisnik_id")
    private Long korisnikId;

    @Column(name = "datum_promjene", nullable = false, updatable = false)
    private LocalDateTime datumPromjene;

    @PrePersist protected void onCreate() { this.datumPromjene = LocalDateTime.now(); }
}
