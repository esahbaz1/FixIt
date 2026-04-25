package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"fotografije", "komentari", "historija", "validacije"})
@Entity
@Table(name = "prijava")
public class Prijava {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Naslov prijave ne smije biti prazan")
    @Column(nullable = false, length = 200)
    private String naslov;

    @Column(columnDefinition = "TEXT")
    private String opis;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(length = 300)
    private String adresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private Statusi status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioritetPrijave prioritet = PrioritetPrijave.SREDNJI;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kategorija_id", nullable = false)
    private Kategorija kategorija;

    @Column(name = "korisnik_id", nullable = false)
    private Long korisnikId;

    @Column(name = "grd_sluzba_id")
    private Long grdSluzbald;

    @Column(name = "datum_podnosenja", nullable = false, updatable = false)
    private LocalDateTime datumPodnosenja;

    @Column(name = "datum_roka")
    private LocalDateTime datumRoka;

    @Column(name = "datum_zavrsetka")
    private LocalDateTime datumZavrsetka;

    @Column(nullable = false)
    private Boolean arhiviran = false;

    @OneToMany(mappedBy = "prijava", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Fotografija> fotografije = new ArrayList<>();

    @OneToMany(mappedBy = "prijava", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Komentar> komentari = new ArrayList<>();

    @OneToMany(mappedBy = "prijava", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorijaPrijave> historija = new ArrayList<>();

    @OneToMany(mappedBy = "prijava", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Validacija> validacije = new ArrayList<>();

    @PrePersist
    protected void onCreate() { this.datumPodnosenja = LocalDateTime.now(); }
}
