package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tip_promjene")
public class TipPromjene {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "status1", length = 100)
    private String status1;
    @Column(name = "status2", length = 100)
    private String status2;
}
