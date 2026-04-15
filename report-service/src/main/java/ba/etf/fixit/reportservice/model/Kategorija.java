package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "kategorija")
public class Kategorija {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Column(nullable = false, unique = true, length = 100)
    private String naziv;
    @Column(length = 300)
    private String opis;
    @Column(name = "gradska_sluzba_id")
    private Long gradskaSluzbaId;

    public Kategorija() {}
    public Kategorija(String naziv, String opis, Long gradskaSluzbaId) {
        this.naziv = naziv; this.opis = opis; this.gradskaSluzbaId = gradskaSluzbaId;
    }
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getNaziv() { return naziv; } public void setNaziv(String v) { this.naziv = v; }
    public String getOpis() { return opis; } public void setOpis(String v) { this.opis = v; }
    public Long getGradskaSluzbaId() { return gradskaSluzbaId; }
    public void setGradskaSluzbaId(Long v) { this.gradskaSluzbaId = v; }
}
