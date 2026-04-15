package ba.etf.fixit.reportservice.repository;
import ba.etf.fixit.reportservice.model.Statusi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StatusiRepository extends JpaRepository<Statusi, Long> {
    Optional<Statusi> findByNaziv(String naziv);
    boolean existsByNaziv(String naziv);
}
