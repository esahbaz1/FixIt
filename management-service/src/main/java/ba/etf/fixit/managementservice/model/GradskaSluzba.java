package ba.etf.fixit.managementservice.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "gradska_sluzba")
public class GradskaSluzba {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Naziv sluzbe ne smije biti prazan")
    @Column(nullable = false, unique = true, length = 150)
    private String naziv;
    @Column(length = 300)
    private String opis;
    @Column(name = "kontakt_email", length = 150)
    private String kontaktEmail;
    @Column(name = "kontakt_telefon", length = 20)
    private String kontaktTelefon;
    @Column(nullable = false)
    private Boolean aktivan = true;

    public GradskaSluzba() {}
    public GradskaSluzba(String naziv, String opis, String email, String telefon) {
        this.naziv=naziv; this.opis=opis; this.kontaktEmail=email; this.kontaktTelefon=telefon; this.aktivan=true;
    }
    public Long getId() { return id; } public void setId(Long v) { this.id=v; }
    public String getNaziv() { return naziv; } public void setNaziv(String v) { this.naziv=v; }
    public String getOpis() { return opis; } public void setOpis(String v) { this.opis=v; }
    public String getKontaktEmail() { return kontaktEmail; } public void setKontaktEmail(String v) { this.kontaktEmail=v; }
    public String getKontaktTelefon() { return kontaktTelefon; } public void setKontaktTelefon(String v) { this.kontaktTelefon=v; }
    public Boolean getAktivan() { return aktivan; } public void setAktivan(Boolean v) { this.aktivan=v; }
}
