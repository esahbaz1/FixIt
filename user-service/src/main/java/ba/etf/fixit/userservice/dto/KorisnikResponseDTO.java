package ba.etf.fixit.userservice.dto;
import ba.etf.fixit.userservice.model.UlogaKorisnika;
import java.time.LocalDateTime;

/** DTO odgovor - NIKAD ne vracamo lozinku! */
public class KorisnikResponseDTO {
    private Long id;
    private String ime;
    private String prezime;
    private String email;
    private UlogaKorisnika uloga;
    private Boolean aktivan;
    private LocalDateTime datumKreiranja;

    public KorisnikResponseDTO() {}
    public KorisnikResponseDTO(Long id, String ime, String prezime, String email,
                                UlogaKorisnika uloga, Boolean aktivan, LocalDateTime datum) {
        this.id=id; this.ime=ime; this.prezime=prezime; this.email=email;
        this.uloga=uloga; this.aktivan=aktivan; this.datumKreiranja=datum;
    }
    public Long getId() { return id; } public void setId(Long v) { this.id=v; }
    public String getIme() { return ime; } public void setIme(String v) { this.ime=v; }
    public String getPrezime() { return prezime; } public void setPrezime(String v) { this.prezime=v; }
    public String getEmail() { return email; } public void setEmail(String v) { this.email=v; }
    public UlogaKorisnika getUloga() { return uloga; } public void setUloga(UlogaKorisnika v) { this.uloga=v; }
    public Boolean getAktivan() { return aktivan; } public void setAktivan(Boolean v) { this.aktivan=v; }
    public LocalDateTime getDatumKreiranja() { return datumKreiranja; }
    public void setDatumKreiranja(LocalDateTime v) { this.datumKreiranja=v; }
}
