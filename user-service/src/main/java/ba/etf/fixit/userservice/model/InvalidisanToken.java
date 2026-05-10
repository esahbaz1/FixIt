package ba.etf.fixit.userservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invalidisan_token",
       indexes = @Index(name = "idx_token_hash", columnList = "token_hash", unique = true))
public class InvalidisanToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "datum_invalidacije", nullable = false)
    private LocalDateTime datumInvalidacije;

    @Column(name = "datum_isteka", nullable = false)
    private LocalDateTime datumIsteka;

    @Column(name = "email_korisnika", length = 150)
    private String emailKorisnika;

    protected InvalidisanToken() {}

    public InvalidisanToken(String tokenHash, LocalDateTime datumIsteka, String emailKorisnika) {
        this.tokenHash = tokenHash;
        this.datumInvalidacije = LocalDateTime.now();
        this.datumIsteka = datumIsteka;
        this.emailKorisnika = emailKorisnika;
    }

    public Long getId() { return id; }
    public String getTokenHash() { return tokenHash; }
    public LocalDateTime getDatumInvalidacije() { return datumInvalidacije; }
    public LocalDateTime getDatumIsteka() { return datumIsteka; }
    public String getEmailKorisnika() { return emailKorisnika; }
}
