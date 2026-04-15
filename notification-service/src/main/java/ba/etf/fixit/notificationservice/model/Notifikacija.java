package ba.etf.fixit.notificationservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Notifikacija koja se salje korisniku sistema.
 * Podrzava in-app i email notifikacije.
 *
 * korisnikId - referenca na Korisnik iz user-service (bez FK).
 * prijavaId  - referenca na Prijava iz report-service (bez FK).
 */
@Entity
@Table(name = "notifikacija")
public class Notifikacija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "korisnik_id", nullable = false)
    private Long korisnikId;

    @Column(name = "prijava_id")
    private Long prijavaId;

    @NotBlank(message = "Naslov notifikacije ne smije biti prazan")
    @Column(nullable = false, length = 200)
    private String naslov;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String tekst;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipNotifikacije tip;

    @Column(nullable = false)
    private Boolean procitano = false;

    @Column(name = "email_poslano", nullable = false)
    private Boolean emailPoslano = false;

    @Column(name = "datum_kreiranja", nullable = false, updatable = false)
    private LocalDateTime datumKreiranja;

    @Column(name = "datum_citanja")
    private LocalDateTime datumCitanja;

    @PrePersist
    protected void onCreate() {
        this.datumKreiranja = LocalDateTime.now();
    }

    // ---- Konstruktori ----
    public Notifikacija() {}

    public Notifikacija(Long korisnikId, Long prijavaId, String naslov,
                        String tekst, TipNotifikacije tip) {
        this.korisnikId = korisnikId;
        this.prijavaId = prijavaId;
        this.naslov = naslov;
        this.tekst = tekst;
        this.tip = tip;
        this.procitano = false;
        this.emailPoslano = false;
    }

    // ---- Getteri i setteri ----
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public Long getKorisnikId() { return korisnikId; } public void setKorisnikId(Long v) { this.korisnikId = v; }
    public Long getPrijavaId() { return prijavaId; } public void setPrijavaId(Long v) { this.prijavaId = v; }
    public String getNaslov() { return naslov; } public void setNaslov(String v) { this.naslov = v; }
    public String getTekst() { return tekst; } public void setTekst(String v) { this.tekst = v; }
    public TipNotifikacije getTip() { return tip; } public void setTip(TipNotifikacije v) { this.tip = v; }
    public Boolean getProcitano() { return procitano; } public void setProcitano(Boolean v) { this.procitano = v; }
    public Boolean getEmailPoslano() { return emailPoslano; } public void setEmailPoslano(Boolean v) { this.emailPoslano = v; }
    public LocalDateTime getDatumKreiranja() { return datumKreiranja; }
    public LocalDateTime getDatumCitanja() { return datumCitanja; }
    public void setDatumCitanja(LocalDateTime v) { this.datumCitanja = v; }
}
