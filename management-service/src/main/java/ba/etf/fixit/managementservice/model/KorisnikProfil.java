package ba.etf.fixit.managementservice.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Profil korisnika u management-service.
 * korisnikId - referenca na Korisnik iz user-service (bez FK).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "korisnik_profil")
public class KorisnikProfil {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "korisnik_id", nullable = false, unique = true)
    private Long korisnikId;

    @Column(length = 20)
    private String telefon;

    @Column(length = 300)
    private String adresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UlogaKorisnika uloga = UlogaKorisnika.GRADJANIN;

    @Column(nullable = false)
    private Boolean aktivan = true;
}
