package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fotografija")
public class Fotografija {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prijava_id", nullable = false)
    private Prijava prijava;

    @Column(nullable = false, length = 500)
    private String putanja;

    @Column(name = "datum_unosa", nullable = false, updatable = false)
    private LocalDateTime datumUnosa;

    @PrePersist protected void onCreate() { this.datumUnosa = LocalDateTime.now(); }
}
