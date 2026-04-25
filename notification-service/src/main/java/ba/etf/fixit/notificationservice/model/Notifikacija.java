package ba.etf.fixit.notificationservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Notifikacija koja se salje korisniku sistema.
 * Podrzava in-app i email notifikacije.
 *
 * korisnikId - referenca na Korisnik iz user-service (bez FK).
 * prijavaId  - referenca na Prijava iz report-service (bez FK).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifikacija")
public class Notifikacija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "korisnik_id", nullable = false)
    private Long korisnikId;

    @Column(name = "prijava_id")
    private Long prijavaId;

    @NotBlank(message = "Naslov notifikacije ne smije biti prazan")
    @Column(nullable = false, length = 200)
    private String naslov;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String tekst;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipNotifikacije tip;

    @Column(nullable = false)
    private Boolean procitano = false;

    @Column(name = "email_poslano", nullable = false)
    private Boolean emailPoslano = false;

    @Column(name = "datum_kreiranja", nullable = false, updatable = false)
    private LocalDateTime datumKreiranja;

    @Column(name = "datum_citanja")
    private LocalDateTime datumCitanja;

    @PrePersist
    protected void onCreate() {
        this.datumKreiranja = LocalDateTime.now();
    }
}
