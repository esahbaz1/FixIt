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
@Table(name = "validacija",
       uniqueConstraints = @UniqueConstraint(columnNames = {"prijava_id", "korisnik_id"}))
public class Validacija {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prijava_id", nullable = false)
    private Prijava prijava;

    @Column(name = "korisnik_id", nullable = false)
    private Long korisnikId;

    @Column(nullable = false)
    private Boolean potvrdjeno;

    @Column(name = "datum_validacije", nullable = false, updatable = false)
    private LocalDateTime datumValidacije;

    @PrePersist protected void onCreate() { this.datumValidacije = LocalDateTime.now(); }
}
