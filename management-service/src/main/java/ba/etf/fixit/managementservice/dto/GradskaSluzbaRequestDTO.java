package ba.etf.fixit.managementservice.dto;
import jakarta.validation.constraints.*;
public class GradskaSluzbaRequestDTO {
    @NotBlank(message = "Naziv sluzbe ne smije biti prazan")
    private String naziv;
    private String opis;
    @Email(message = "Kontakt email mora biti validan")
    private String kontaktEmail;
    private String kontaktTelefon;
    public String getNaziv(){return naziv;} public void setNaziv(String v){this.naziv=v;}
    public String getOpis(){return opis;} public void setOpis(String v){this.opis=v;}
    public String getKontaktEmail(){return kontaktEmail;} public void setKontaktEmail(String v){this.kontaktEmail=v;}
    public String getKontaktTelefon(){return kontaktTelefon;} public void setKontaktTelefon(String v){this.kontaktTelefon=v;}
}
