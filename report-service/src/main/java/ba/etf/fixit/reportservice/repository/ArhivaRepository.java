package ba.etf.fixit.reportservice.repository;
import ba.etf.fixit.reportservice.model.Arhiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ArhivaRepository extends JpaRepository<Arhiva, Long> {
    Optional<Arhiva> findByPrijavaId(Long prijavaId);
    boolean existsByPrijavaId(Long prijavaId);
}
