package ba.etf.fixit.reportservice.model;
import jakarta.persistence.*;

@Entity
@Table(name = "tip_promjene")
public class TipPromjene {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "status1", length = 100)
    private String status1;
    @Column(name = "status2", length = 100)
    private String status2;

    public TipPromjene() {}
    public TipPromjene(String status1, String status2) { this.status1 = status1; this.status2 = status2; }
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getStatus1() { return status1; } public void setStatus1(String v) { this.status1 = v; }
    public String getStatus2() { return status2; } public void setStatus2(String v) { this.status2 = v; }
}
