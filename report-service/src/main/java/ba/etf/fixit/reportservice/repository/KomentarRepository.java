package ba.etf.fixit.reportservice.repository;
import ba.etf.fixit.reportservice.model.Komentar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KomentarRepository extends JpaRepository<Komentar, Long> {
    List<Komentar> findByPrijavaIdAndInteranFalse(Long prijavaId);
    List<Komentar> findByPrijavaId(Long prijavaId);
}
