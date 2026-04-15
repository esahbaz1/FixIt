package ba.etf.fixit.notificationservice.dto;
import ba.etf.fixit.notificationservice.model.TipNotifikacije;
import jakarta.validation.constraints.*;
public class NotifikacijaRequestDTO {
    @NotNull(message = "Korisnik ID je obavezan") private Long korisnikId;
    private Long prijavaId;
    @NotBlank(message = "Naslov ne smije biti prazan") private String naslov;
    @NotBlank(message = "Tekst ne smije biti prazan") private String tekst;
    @NotNull(message = "Tip notifikacije je obavezan") private TipNotifikacije tip;
    public Long getKorisnikId(){return korisnikId;} public void setKorisnikId(Long v){this.korisnikId=v;}
    public Long getPrijavaId(){return prijavaId;} public void setPrijavaId(Long v){this.prijavaId=v;}
    public String getNaslov(){return naslov;} public void setNaslov(String v){this.naslov=v;}
    public String getTekst(){return tekst;} public void setTekst(String v){this.tekst=v;}
    public TipNotifikacije getTip(){return tip;} public void setTip(TipNotifikacije v){this.tip=v;}
}
