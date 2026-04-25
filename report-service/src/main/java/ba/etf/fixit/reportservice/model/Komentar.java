package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "komentar")
public class Komentar {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "korisnik_id", nullable = false)
    private Long korisnikId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prijava_id", nullable = false)
    private Prijava prijava;

    @Column(length = 200)
    private String naslov;

    @NotBlank(message = "Tekst komentara ne smije biti prazan")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String tekst;

    @Column(nullable = false)
    private Boolean interan = false;

    @Column(name = "datum_kreiranja", nullable = false, updatable = false)
    private LocalDateTime datumKreiranja;

    @PrePersist protected void onCreate() { this.datumKreiranja = LocalDateTime.now(); }
}
