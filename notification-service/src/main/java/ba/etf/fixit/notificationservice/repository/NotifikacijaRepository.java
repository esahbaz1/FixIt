package ba.etf.fixit.notificationservice.repository;
import ba.etf.fixit.notificationservice.model.Notifikacija;
import ba.etf.fixit.notificationservice.model.TipNotifikacije;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface NotifikacijaRepository extends JpaRepository<Notifikacija, Long> {
    List<Notifikacija> findByKorisnikIdOrderByDatumKreiranjaDesc(Long korisnikId);
    List<Notifikacija> findByKorisnikIdAndProcitanoFalse(Long korisnikId);
    long countByKorisnikIdAndProcitanoFalse(Long korisnikId);
    List<Notifikacija> findByPrijavaId(Long prijavaId);
    Page<Notifikacija> findByKorisnikId(Long korisnikId, Pageable pageable);



@Query("""
SELECT n FROM Notifikacija n
WHERE n.korisnikId = :korisnikId
AND n.procitano = false
AND n.tip = :tip
ORDER BY n.datumKreiranja DESC
""")
List<Notifikacija> findByKorisnikIdAndProcitanoFalseAndTip(Long korisnikId, TipNotifikacije tip);
}
