package ba.etf.fixit.managementservice.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

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

    public Radnik() {}
    public Radnik(Long korisnikId, GradskaSluzba sluzba, String pozicija, String kompetencije) {
        this.korisnikId=korisnikId; this.gradskaSluzba=sluzba;
        this.pozicija=pozicija; this.kompetencije=kompetencije; this.aktivan=true;
    }
    public Long getId() { return id; } public void setId(Long v) { this.id=v; }
    public Long getKorisnikId() { return korisnikId; } public void setKorisnikId(Long v) { this.korisnikId=v; }
    public GradskaSluzba getGradskaSluzba() { return gradskaSluzba; } public void setGradskaSluzba(GradskaSluzba v) { this.gradskaSluzba=v; }
    public String getPozicija() { return pozicija; } public void setPozicija(String v) { this.pozicija=v; }
    public String getKompetencije() { return kompetencije; } public void setKompetencije(String v) { this.kompetencije=v; }
    public Boolean getAktivan() { return aktivan; } public void setAktivan(Boolean v) { this.aktivan=v; }
}
