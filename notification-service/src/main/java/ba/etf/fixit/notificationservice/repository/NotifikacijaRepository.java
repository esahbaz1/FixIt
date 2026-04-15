package ba.etf.fixit.notificationservice.repository;
import ba.etf.fixit.notificationservice.model.Notifikacija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotifikacijaRepository extends JpaRepository<Notifikacija, Long> {
    List<Notifikacija> findByKorisnikIdOrderByDatumKreiranjaDesc(Long korisnikId);
    List<Notifikacija> findByKorisnikIdAndProcitanoFalse(Long korisnikId);
    long countByKorisnikIdAndProcitanoFalse(Long korisnikId);
    List<Notifikacija> findByPrijavaId(Long prijavaId);
}
