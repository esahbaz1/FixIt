package ba.etf.fixit.reportservice.repository;
import ba.etf.fixit.reportservice.model.Kategorija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface KategorijaRepository extends JpaRepository<Kategorija, Long> {
    Optional<Kategorija> findByNaziv(String naziv);
    boolean existsByNaziv(String naziv);
}
