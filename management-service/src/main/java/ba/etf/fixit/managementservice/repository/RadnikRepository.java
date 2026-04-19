package ba.etf.fixit.managementservice.repository;
import ba.etf.fixit.managementservice.model.Radnik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

 import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
@Repository
public interface RadnikRepository extends JpaRepository<Radnik, Long> {
    List<Radnik> findByGradskaSluzbaId(Long gradskaSluzbaId);
    Page<Radnik> findByGradskaSluzbaId(Long sluzbaId, Pageable pageable);
    List<Radnik> findByAktivanTrue();
   

@Query("""
SELECT r FROM Radnik r
WHERE r.gradskaSluzba.id = :sluzbaId
AND r.aktivan = true
AND r.pozicija = :pozicija
ORDER BY r.id DESC
""")
List<Radnik> findAktivniPoPoziciji(
        @Param("sluzbaId") Long sluzbaId,
        @Param("pozicija") String pozicija
);

    @Query("SELECT COUNT(r) FROM Radnik r WHERE r.gradskaSluzba.id = :sluzbaId AND r.aktivan = true")
    long countAktivnihPoSluzbi(Long sluzbaId);
}
