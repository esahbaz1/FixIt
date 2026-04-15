package ba.etf.fixit.reportservice.dto;
import jakarta.validation.constraints.*;
public class KomentarRequestDTO {
    @NotNull(message = "Korisnik ID je obavezan") private Long korisnikId;
    private String naslov;
    @NotBlank(message = "Tekst ne smije biti prazan") private String tekst;
    private Boolean interan = false;
    public Long getKorisnikId(){return korisnikId;} public void setKorisnikId(Long v){this.korisnikId=v;}
    public String getNaslov(){return naslov;} public void setNaslov(String v){this.naslov=v;}
    public String getTekst(){return tekst;} public void setTekst(String v){this.tekst=v;}
    public Boolean getInteran(){return interan;} public void setInteran(Boolean v){this.interan=v;}
}
