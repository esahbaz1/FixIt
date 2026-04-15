package ba.etf.fixit.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Korisnik sistema - cuva podatke za autentifikaciju.
 * Profil i uloge se upravljaju u management-service.
 */
@Entity
@Table(name = "korisnik")
public class Korisnik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ime ne smije biti prazno")
    @Column(nullable = false, length = 100)
    private String ime;

    @NotBlank(message = "Prezime ne smije biti prazno")
    @Column(nullable = false, length = 100)
    private String prezime;

    @Email(message = "Email mora biti u ispravnom formatu")
    @NotBlank(message = "Email ne smije biti prazan")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Lozinka ne smije biti prazna")
    @Size(min = 8, message = "Lozinka mora imati najmanje 8 karaktera")
    @Column(nullable = false)
    private String lozinka;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UlogaKorisnika uloga = UlogaKorisnika.GRADJANIN;

    @Column(nullable = false)
    private Boolean aktivan = true;

    @Column(name = "datum_kreiranja", nullable = false, updatable = false)
    private LocalDateTime datumKreiranja;

    @PrePersist
    protected void onCreate() { this.datumKreiranja = LocalDateTime.now(); }

    public Korisnik() {}
    public Korisnik(String ime, String prezime, String email, String lozinka, UlogaKorisnika uloga) {
        this.ime = ime; this.prezime = prezime; this.email = email;
        this.lozinka = lozinka; this.uloga = uloga; this.aktivan = true;
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getIme() { return ime; } public void setIme(String v) { this.ime = v; }
    public String getPrezime() { return prezime; } public void setPrezime(String v) { this.prezime = v; }
    public String getEmail() { return email; } public void setEmail(String v) { this.email = v; }
    public String getLozinka() { return lozinka; } public void setLozinka(String v) { this.lozinka = v; }
    public UlogaKorisnika getUloga() { return uloga; } public void setUloga(UlogaKorisnika v) { this.uloga = v; }
    public Boolean getAktivan() { return aktivan; } public void setAktivan(Boolean v) { this.aktivan = v; }
    public LocalDateTime getDatumKreiranja() { return datumKreiranja; }
}
