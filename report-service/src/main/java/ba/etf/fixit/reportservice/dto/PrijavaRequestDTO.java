package ba.etf.fixit.reportservice.dto;
import ba.etf.fixit.reportservice.model.PrioritetPrijave;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
public class PrijavaRequestDTO {
    @NotBlank(message = "Naslov ne smije biti prazan") private String naslov;
    @NotBlank(message = "Opis ne smije biti prazan") private String opis;
    @NotNull(message = "Latitude je obavezna") private Double latitude;
    @NotNull(message = "Longitude je obavezna") private Double longitude;
    private String adresa;
    @NotNull(message = "Kategorija ID je obavezna") private Long kategorijaId;
    @NotNull(message = "Korisnik ID je obavezan") private Long korisnikId;
    private PrioritetPrijave prioritet;
    private Long statusId;
    private LocalDateTime datumRoka;
    public String getNaslov(){return naslov;} public void setNaslov(String v){this.naslov=v;}
    public String getOpis(){return opis;} public void setOpis(String v){this.opis=v;}
    public Double getLatitude(){return latitude;} public void setLatitude(Double v){this.latitude=v;}
    public Double getLongitude(){return longitude;} public void setLongitude(Double v){this.longitude=v;}
    public String getAdresa(){return adresa;} public void setAdresa(String v){this.adresa=v;}
    public Long getKategorijaId(){return kategorijaId;} public void setKategorijaId(Long v){this.kategorijaId=v;}
    public Long getKorisnikId(){return korisnikId;} public void setKorisnikId(Long v){this.korisnikId=v;}
    public PrioritetPrijave getPrioritet(){return prioritet;} public void setPrioritet(PrioritetPrijave v){this.prioritet=v;}
    public Long getStatusId(){return statusId;} public void setStatusId(Long v){this.statusId=v;}
    public LocalDateTime getDatumRoka(){return datumRoka;} public void setDatumRoka(LocalDateTime v){this.datumRoka=v;}
}
