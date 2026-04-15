package ba.etf.fixit.managementservice.repository;
import ba.etf.fixit.managementservice.model.GradskaSluzba;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface GradskaSluzbaRepository extends JpaRepository<GradskaSluzba, Long> {
    Optional<GradskaSluzba> findByNaziv(String naziv);
    boolean existsByNaziv(String naziv);
}
