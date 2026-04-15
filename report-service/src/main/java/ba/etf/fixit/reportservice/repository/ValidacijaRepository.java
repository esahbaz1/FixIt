package ba.etf.fixit.reportservice.repository;
import ba.etf.fixit.reportservice.model.Validacija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ValidacijaRepository extends JpaRepository<Validacija, Long> {
    Optional<Validacija> findByPrijavaIdAndKorisnikId(Long prijavaId, Long korisnikId);
    boolean existsByPrijavaIdAndKorisnikId(Long prijavaId, Long korisnikId);
    long countByPrijavaIdAndPotvrdjenoTrue(Long prijavaId);
}
