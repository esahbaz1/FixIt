package ba.etf.fixit.reportservice.dto;
import java.time.LocalDateTime;
public class KomentarResponseDTO {
    private Long id; private Long prijavaId; private Long korisnikId;
    private String naslov; private String tekst; private Boolean interan; private LocalDateTime datumKreiranja;
    public KomentarResponseDTO(){}
    public Long getId(){return id;} public void setId(Long v){this.id=v;}
    public Long getPrijavaId(){return prijavaId;} public void setPrijavaId(Long v){this.prijavaId=v;}
    public Long getKorisnikId(){return korisnikId;} public void setKorisnikId(Long v){this.korisnikId=v;}
    public String getNaslov(){return naslov;} public void setNaslov(String v){this.naslov=v;}
    public String getTekst(){return tekst;} public void setTekst(String v){this.tekst=v;}
    public Boolean getInteran(){return interan;} public void setInteran(Boolean v){this.interan=v;}
    public LocalDateTime getDatumKreiranja(){return datumKreiranja;} public void setDatumKreiranja(LocalDateTime v){this.datumKreiranja=v;}
}
