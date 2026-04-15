package ba.etf.fixit.reportservice.repository;
import ba.etf.fixit.reportservice.model.TipPromjene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TipPromjeneRepository extends JpaRepository<TipPromjene, Long> {
    Optional<TipPromjene> findByStatus1AndStatus2(String status1, String status2);
}
