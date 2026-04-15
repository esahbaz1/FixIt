package ba.etf.fixit.userservice.dto;
import ba.etf.fixit.userservice.model.UlogaKorisnika;

public class LoginResponseDTO {
    private Long id;
    private String email;
    private String ime;
    private String prezime;
    private UlogaKorisnika uloga;
    private String poruka;

    public LoginResponseDTO() {}
    public LoginResponseDTO(Long id, String email, String ime, String prezime,
                             UlogaKorisnika uloga, String poruka) {
        this.id=id; this.email=email; this.ime=ime; this.prezime=prezime;
        this.uloga=uloga; this.poruka=poruka;
    }
    public Long getId() { return id; } public void setId(Long v) { this.id=v; }
    public String getEmail() { return email; } public void setEmail(String v) { this.email=v; }
    public String getIme() { return ime; } public void setIme(String v) { this.ime=v; }
    public String getPrezime() { return prezime; } public void setPrezime(String v) { this.prezime=v; }
    public UlogaKorisnika getUloga() { return uloga; } public void setUloga(UlogaKorisnika v) { this.uloga=v; }
    public String getPoruka() { return poruka; } public void setPoruka(String v) { this.poruka=v; }
}
