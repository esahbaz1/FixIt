package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;

@Entity
@Table(name = "arhiva")
public class Arhiva {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prijava_id", nullable = false, unique = true)
    private Prijava prijava;

    @Column(name = "trajanje_prijave")
    private Long trajanjePrijave;

    public Arhiva() {}
    public Arhiva(Prijava prijava, Long trajanjePrijave) {
        this.prijava = prijava; this.trajanjePrijave = trajanjePrijave;
    }
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public Prijava getPrijava() { return prijava; } public void setPrijava(Prijava v) { this.prijava = v; }
    public Long getTrajanjePrijave() { return trajanjePrijave; }
    public void setTrajanjePrijave(Long v) { this.trajanjePrijave = v; }
}
