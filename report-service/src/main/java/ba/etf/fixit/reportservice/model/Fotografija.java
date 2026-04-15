package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fotografija")
public class Fotografija {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prijava_id", nullable = false)
    private Prijava prijava;

    @Column(nullable = false, length = 500)
    private String putanja;

    @Column(name = "datum_unosa", nullable = false, updatable = false)
    private LocalDateTime datumUnosa;

    @PrePersist protected void onCreate() { this.datumUnosa = LocalDateTime.now(); }

    public Fotografija() {}
    public Fotografija(Prijava prijava, String putanja) { this.prijava = prijava; this.putanja = putanja; }
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public Prijava getPrijava() { return prijava; } public void setPrijava(Prijava v) { this.prijava = v; }
    public String getPutanja() { return putanja; } public void setPutanja(String v) { this.putanja = v; }
    public LocalDateTime getDatumUnosa() { return datumUnosa; }
}
