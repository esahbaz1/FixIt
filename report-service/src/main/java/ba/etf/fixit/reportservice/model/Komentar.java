package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "komentar")
public class Komentar {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "korisnik_id", nullable = false)
    private Long korisnikId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prijava_id", nullable = false)
    private Prijava prijava;

    @Column(length = 200)
    private String naslov;

    @NotBlank(message = "Tekst komentara ne smije biti prazan")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String tekst;

    @Column(nullable = false)
    private Boolean interan = false;

    @Column(name = "datum_kreiranja", nullable = false, updatable = false)
    private LocalDateTime datumKreiranja;

    @PrePersist protected void onCreate() { this.datumKreiranja = LocalDateTime.now(); }

    public Komentar() {}
    public Komentar(Long korisnikId, Prijava prijava, String naslov, String tekst, Boolean interan) {
        this.korisnikId = korisnikId; this.prijava = prijava;
        this.naslov = naslov; this.tekst = tekst; this.interan = interan;
    }
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public Long getKorisnikId() { return korisnikId; } public void setKorisnikId(Long v) { this.korisnikId = v; }
    public Prijava getPrijava() { return prijava; } public void setPrijava(Prijava v) { this.prijava = v; }
    public String getNaslov() { return naslov; } public void setNaslov(String v) { this.naslov = v; }
    public String getTekst() { return tekst; } public void setTekst(String v) { this.tekst = v; }
    public Boolean getInteran() { return interan; } public void setInteran(Boolean v) { this.interan = v; }
    public LocalDateTime getDatumKreiranja() { return datumKreiranja; }
}
