package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    public HistorijaPrijave() {}
    public HistorijaPrijave(TipPromjene tipPromjene, Prijava prijava, Long korisnikId) {
        this.tipPromjene = tipPromjene; this.prijava = prijava; this.korisnikId = korisnikId;
    }
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public TipPromjene getTipPromjene() { return tipPromjene; } public void setTipPromjene(TipPromjene v) { this.tipPromjene = v; }
    public Prijava getPrijava() { return prijava; } public void setPrijava(Prijava v) { this.prijava = v; }
    public Long getKorisnikId() { return korisnikId; } public void setKorisnikId(Long v) { this.korisnikId = v; }
    public LocalDateTime getDatumPromjene() { return datumPromjene; }
}
