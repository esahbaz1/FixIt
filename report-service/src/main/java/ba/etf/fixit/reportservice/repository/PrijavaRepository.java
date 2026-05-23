package ba.etf.fixit.reportservice.repository;

import ba.etf.fixit.reportservice.model.Prijava;
import ba.etf.fixit.reportservice.model.PrioritetPrijave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrijavaRepository extends JpaRepository<Prijava, Long> {

    List<Prijava> findByKorisnikId(Long korisnikId);
    List<Prijava> findByStatusId(Long statusId);
    List<Prijava> findByPrioritet(PrioritetPrijave prioritet);
    List<Prijava> findByKategorijaId(Long kategorijaId);
    List<Prijava> findByArhiviranFalse();
    List<Prijava> findByArhiviranTrue();

    Page<Prijava> findByArhiviranFalse(Pageable pageable);

    @Query("SELECT p FROM Prijava p WHERE LOWER(p.naslov) LIKE LOWER(CONCAT('%',:k,'%')) OR LOWER(p.opis) LIKE LOWER(CONCAT('%',:k,'%'))")
    List<Prijava> pretraziPoKljucnojRijeci(@Param("k") String kljucnaRijec);

    @Query("SELECT p FROM Prijava p WHERE p.datumPodnosenja BETWEEN :od AND :do AND p.arhiviran = false")
    List<Prijava> findByVremenskiPeriod(@Param("od") LocalDateTime od, @Param("do") LocalDateTime do_);

    @Query("SELECT p FROM Prijava p WHERE LOWER(p.adresa) LIKE LOWER(CONCAT('%',:l,'%')) AND p.arhiviran = false")
    List<Prijava> findByLokacija(@Param("l") String lokacija);

    @Query("SELECT p FROM Prijava p WHERE p.datumRoka < :sada AND p.arhiviran = false")
    List<Prijava> findPrekoraceniRokovi(@Param("sada") LocalDateTime sada);

    @Query("SELECT p FROM Prijava p WHERE p.arhiviran = false AND p.latitude IS NOT NULL AND p.longitude IS NOT NULL")
    List<Prijava> findZaHeatmap();

    @Query("SELECT p FROM Prijava p WHERE p.prioritet = 'HITNO' AND p.datumRoka < :sada AND p.arhiviran = false")
    List<Prijava> findHitneSaPrekoracenimRokom(@Param("sada") LocalDateTime sada);

    @Query("SELECT p.kategorija.naziv, COUNT(p) FROM Prijava p GROUP BY p.kategorija.naziv")
    List<Object[]> countPoKategorijama();

    @Query("SELECT p.status.naziv, COUNT(p) FROM Prijava p WHERE p.arhiviran = false GROUP BY p.status.naziv")
    List<Object[]> countPoStatusima();

    @Query("SELECT FUNCTION('DATE_FORMAT', p.datumPodnosenja, '%Y-%m'), COUNT(p) " +
           "FROM Prijava p WHERE p.datumPodnosenja >= :od GROUP BY FUNCTION('DATE_FORMAT', p.datumPodnosenja, '%Y-%m') " +
           "ORDER BY FUNCTION('DATE_FORMAT', p.datumPodnosenja, '%Y-%m')")
    List<Object[]> countPoMjesecima(@Param("od") LocalDateTime od);

    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, p.datumPodnosenja, p.datumZavrsetka)) FROM Prijava p WHERE p.datumZavrsetka IS NOT NULL")
    Double prosjecnoVrijemeRjesavanjaH();
}
