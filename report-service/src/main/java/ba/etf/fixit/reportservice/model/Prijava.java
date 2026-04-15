package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public Prijava() {}
    public Prijava(String naslov, String opis, Double latitude, Double longitude,
                   String adresa, Kategorija kategorija, Long korisnikId, Statusi status) {
        this.naslov = naslov; this.opis = opis; this.latitude = latitude;
        this.longitude = longitude; this.adresa = adresa; this.kategorija = kategorija;
        this.korisnikId = korisnikId; this.status = status; this.arhiviran = false;
    }

    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getNaslov() { return naslov; } public void setNaslov(String v) { this.naslov = v; }
    public String getOpis() { return opis; } public void setOpis(String v) { this.opis = v; }
    public Double getLatitude() { return latitude; } public void setLatitude(Double v) { this.latitude = v; }
    public Double getLongitude() { return longitude; } public void setLongitude(Double v) { this.longitude = v; }
    public String getAdresa() { return adresa; } public void setAdresa(String v) { this.adresa = v; }
    public Statusi getStatus() { return status; } public void setStatus(Statusi v) { this.status = v; }
    public PrioritetPrijave getPrioritet() { return prioritet; } public void setPrioritet(PrioritetPrijave v) { this.prioritet = v; }
    public Kategorija getKategorija() { return kategorija; } public void setKategorija(Kategorija v) { this.kategorija = v; }
    public Long getKorisnikId() { return korisnikId; } public void setKorisnikId(Long v) { this.korisnikId = v; }
    public Long getGrdSluzbald() { return grdSluzbald; } public void setGrdSluzbald(Long v) { this.grdSluzbald = v; }
    public LocalDateTime getDatumPodnosenja() { return datumPodnosenja; }
    public LocalDateTime getDatumRoka() { return datumRoka; } public void setDatumRoka(LocalDateTime v) { this.datumRoka = v; }
    public LocalDateTime getDatumZavrsetka() { return datumZavrsetka; } public void setDatumZavrsetka(LocalDateTime v) { this.datumZavrsetka = v; }
    public Boolean getArhiviran() { return arhiviran; } public void setArhiviran(Boolean v) { this.arhiviran = v; }
    public List<Fotografija> getFotografije() { return fotografije; }
    public List<Komentar> getKomentari() { return komentari; }
    public List<HistorijaPrijave> getHistorija() { return historija; }
    public List<Validacija> getValidacije() { return validacije; }
}
