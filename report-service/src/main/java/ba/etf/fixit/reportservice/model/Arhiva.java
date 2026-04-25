package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "arhiva")
public class Arhiva {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prijava_id", nullable = false, unique = true)
    private Prijava prijava;

    @Column(name = "trajanje_prijave")
    private Long trajanjePrijave;
}
