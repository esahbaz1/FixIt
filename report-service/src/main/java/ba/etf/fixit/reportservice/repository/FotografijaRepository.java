package ba.etf.fixit.reportservice.repository;
import ba.etf.fixit.reportservice.model.Fotografija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FotografijaRepository extends JpaRepository<Fotografija, Long> {
    List<Fotografija> findByPrijavaId(Long prijavaId);
    long countByPrijavaId(Long prijavaId);
}
