package ba.etf.fixit.managementservice.model;
import jakarta.persistence.*;


/**
 * Profil korisnika u management-service.
 * korisnikId - referenca na Korisnik iz user-service (bez FK).
 */
@Entity
@Table(name = "korisnik_profil")
public class KorisnikProfil {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "korisnik_id", nullable = false, unique = true)
    private Long korisnikId;

    @Column(length = 20)
    private String telefon;

    @Column(length = 300)
    private String adresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UlogaKorisnika uloga = UlogaKorisnika.GRADJANIN;

    @Column(nullable = false)
    private Boolean aktivan = true;

    public KorisnikProfil() {}
    public KorisnikProfil(Long korisnikId, String telefon, String adresa, UlogaKorisnika uloga) {
        this.korisnikId=korisnikId; this.telefon=telefon; this.adresa=adresa; this.uloga=uloga; this.aktivan=true;
    }
    public Long getId() { return id; } public void setId(Long v) { this.id=v; }
    public Long getKorisnikId() { return korisnikId; } public void setKorisnikId(Long v) { this.korisnikId=v; }
    public String getTelefon() { return telefon; } public void setTelefon(String v) { this.telefon=v; }
    public String getAdresa() { return adresa; } public void setAdresa(String v) { this.adresa=v; }
    public UlogaKorisnika getUloga() { return uloga; } public void setUloga(UlogaKorisnika v) { this.uloga=v; }
    public Boolean getAktivan() { return aktivan; } public void setAktivan(Boolean v) { this.aktivan=v; }
}
