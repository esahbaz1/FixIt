package ba.etf.fixit.managementservice.dto;
import ba.etf.fixit.managementservice.model.UlogaKorisnika;
import jakarta.validation.constraints.NotNull;
public class KorisnikProfilRequestDTO {
    @NotNull(message = "Korisnik ID je obavezan")
    private Long korisnikId;
    private String telefon;
    private String adresa;
    private UlogaKorisnika uloga;
    public Long getKorisnikId(){return korisnikId;} public void setKorisnikId(Long v){this.korisnikId=v;}
    public String getTelefon(){return telefon;} public void setTelefon(String v){this.telefon=v;}
    public String getAdresa(){return adresa;} public void setAdresa(String v){this.adresa=v;}
    public UlogaKorisnika getUloga(){return uloga;} public void setUloga(UlogaKorisnika v){this.uloga=v;}
}
