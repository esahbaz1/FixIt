package ba.etf.fixit.notificationservice.dto;
import ba.etf.fixit.notificationservice.model.TipNotifikacije;
import java.time.LocalDateTime;
public class NotifikacijaResponseDTO {
    private Long id; private Long korisnikId; private Long prijavaId;
    private String naslov; private String tekst; private TipNotifikacije tip;
    private Boolean procitano; private Boolean emailPoslano;
    private LocalDateTime datumKreiranja; private LocalDateTime datumCitanja;
    public NotifikacijaResponseDTO(){}
    public Long getId(){return id;} public void setId(Long v){this.id=v;}
    public Long getKorisnikId(){return korisnikId;} public void setKorisnikId(Long v){this.korisnikId=v;}
    public Long getPrijavaId(){return prijavaId;} public void setPrijavaId(Long v){this.prijavaId=v;}
    public String getNaslov(){return naslov;} public void setNaslov(String v){this.naslov=v;}
    public String getTekst(){return tekst;} public void setTekst(String v){this.tekst=v;}
    public TipNotifikacije getTip(){return tip;} public void setTip(TipNotifikacije v){this.tip=v;}
    public Boolean getProcitano(){return procitano;} public void setProcitano(Boolean v){this.procitano=v;}
    public Boolean getEmailPoslano(){return emailPoslano;} public void setEmailPoslano(Boolean v){this.emailPoslano=v;}
    public LocalDateTime getDatumKreiranja(){return datumKreiranja;} public void setDatumKreiranja(LocalDateTime v){this.datumKreiranja=v;}
    public LocalDateTime getDatumCitanja(){return datumCitanja;} public void setDatumCitanja(LocalDateTime v){this.datumCitanja=v;}
}
