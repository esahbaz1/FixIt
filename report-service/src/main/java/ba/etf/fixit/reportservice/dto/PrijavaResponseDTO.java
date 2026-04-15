package ba.etf.fixit.reportservice.dto;
import ba.etf.fixit.reportservice.model.PrioritetPrijave;
import java.time.LocalDateTime;
public class PrijavaResponseDTO {
    private Long id; private String naslov; private String opis;
    private Double latitude; private Double longitude; private String adresa;
    private String statusNaziv; private PrioritetPrijave prioritet;
    private Long kategorijaId; private String nazivKategorije;
    private Long korisnikId; private LocalDateTime datumPodnosenja;
    private LocalDateTime datumRoka; private LocalDateTime datumZavrsetka; private Boolean arhiviran;
    public PrijavaResponseDTO(){}
    public Long getId(){return id;} public void setId(Long v){this.id=v;}
    public String getNaslov(){return naslov;} public void setNaslov(String v){this.naslov=v;}
    public String getOpis(){return opis;} public void setOpis(String v){this.opis=v;}
    public Double getLatitude(){return latitude;} public void setLatitude(Double v){this.latitude=v;}
    public Double getLongitude(){return longitude;} public void setLongitude(Double v){this.longitude=v;}
    public String getAdresa(){return adresa;} public void setAdresa(String v){this.adresa=v;}
    public String getStatusNaziv(){return statusNaziv;} public void setStatusNaziv(String v){this.statusNaziv=v;}
    public PrioritetPrijave getPrioritet(){return prioritet;} public void setPrioritet(PrioritetPrijave v){this.prioritet=v;}
    public Long getKategorijaId(){return kategorijaId;} public void setKategorijaId(Long v){this.kategorijaId=v;}
    public String getNazivKategorije(){return nazivKategorije;} public void setNazivKategorije(String v){this.nazivKategorije=v;}
    public Long getKorisnikId(){return korisnikId;} public void setKorisnikId(Long v){this.korisnikId=v;}
    public LocalDateTime getDatumPodnosenja(){return datumPodnosenja;} public void setDatumPodnosenja(LocalDateTime v){this.datumPodnosenja=v;}
    public LocalDateTime getDatumRoka(){return datumRoka;} public void setDatumRoka(LocalDateTime v){this.datumRoka=v;}
    public LocalDateTime getDatumZavrsetka(){return datumZavrsetka;} public void setDatumZavrsetka(LocalDateTime v){this.datumZavrsetka=v;}
    public Boolean getArhiviran(){return arhiviran;} public void setArhiviran(Boolean v){this.arhiviran=v;}
}
