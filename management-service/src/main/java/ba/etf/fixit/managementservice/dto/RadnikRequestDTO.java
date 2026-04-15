package ba.etf.fixit.managementservice.dto;
import jakarta.validation.constraints.NotNull;
public class RadnikRequestDTO {
    @NotNull(message = "Korisnik ID je obavezan")
    private Long korisnikId;
    @NotNull(message = "Gradska sluzba ID je obavezna")
    private Long gradskaSluzbaId;
    private String pozicija;
    private String kompetencije;
    public Long getKorisnikId(){return korisnikId;} public void setKorisnikId(Long v){this.korisnikId=v;}
    public Long getGradskaSluzbaId(){return gradskaSluzbaId;} public void setGradskaSluzbaId(Long v){this.gradskaSluzbaId=v;}
    public String getPozicija(){return pozicija;} public void setPozicija(String v){this.pozicija=v;}
    public String getKompetencije(){return kompetencije;} public void setKompetencije(String v){this.kompetencije=v;}
}
