package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "statusi")
public class Statusi {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Column(nullable = false, unique = true, length = 100)
    private String naziv;
    @Column(length = 200)
    private String opis;

    public Statusi() {}
    public Statusi(String naziv, String opis) { this.naziv = naziv; this.opis = opis; }
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getNaziv() { return naziv; } public void setNaziv(String v) { this.naziv = v; }
    public String getOpis() { return opis; } public void setOpis(String v) { this.opis = v; }
}
