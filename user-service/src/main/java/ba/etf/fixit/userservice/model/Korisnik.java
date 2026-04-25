package ba.etf.fixit.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Korisnik sistema - cuva podatke za autentifikaciju.
 * Profil i uloge se upravljaju u management-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
