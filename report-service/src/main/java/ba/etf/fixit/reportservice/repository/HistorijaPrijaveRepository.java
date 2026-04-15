package ba.etf.fixit.reportservice.repository;
import ba.etf.fixit.reportservice.model.HistorijaPrijave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorijaPrijaveRepository extends JpaRepository<HistorijaPrijave, Long> {
    List<HistorijaPrijave> findByPrijavaIdOrderByDatumPromjeneAsc(Long prijavaId);
}
