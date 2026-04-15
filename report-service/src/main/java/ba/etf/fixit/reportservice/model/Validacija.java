package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    public Validacija() {}
    public Validacija(Prijava prijava, Long korisnikId, Boolean potvrdjeno) {
        this.prijava = prijava; this.korisnikId = korisnikId; this.potvrdjeno = potvrdjeno;
    }
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public Prijava getPrijava() { return prijava; } public void setPrijava(Prijava v) { this.prijava = v; }
    public Long getKorisnikId() { return korisnikId; } public void setKorisnikId(Long v) { this.korisnikId = v; }
    public Boolean getPotvrdjeno() { return potvrdjeno; } public void setPotvrdjeno(Boolean v) { this.potvrdjeno = v; }
    public LocalDateTime getDatumValidacije() { return datumValidacije; }
}
