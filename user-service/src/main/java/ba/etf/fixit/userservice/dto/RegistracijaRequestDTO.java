package ba.etf.fixit.userservice.dto;
import ba.etf.fixit.userservice.model.UlogaKorisnika;
import jakarta.validation.constraints.*;

public class RegistracijaRequestDTO {
    @NotBlank(message = "Ime ne smije biti prazno")
    private String ime;
    @NotBlank(message = "Prezime ne smije biti prazno")
    private String prezime;
    @Email(message = "Email mora biti u ispravnom formatu")
    @NotBlank(message = "Email ne smije biti prazan")
    private String email;
    @NotBlank(message = "Lozinka ne smije biti prazna")
    @Size(min = 8, message = "Lozinka mora imati najmanje 8 karaktera")
    private String lozinka;
    private UlogaKorisnika uloga;

    public String getIme() { return ime; } public void setIme(String v) { this.ime = v; }
    public String getPrezime() { return prezime; } public void setPrezime(String v) { this.prezime = v; }
    public String getEmail() { return email; } public void setEmail(String v) { this.email = v; }
    public String getLozinka() { return lozinka; } public void setLozinka(String v) { this.lozinka = v; }
    public UlogaKorisnika getUloga() { return uloga; } public void setUloga(UlogaKorisnika v) { this.uloga = v; }
}
