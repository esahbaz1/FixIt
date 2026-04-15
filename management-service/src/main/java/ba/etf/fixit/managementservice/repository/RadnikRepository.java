package ba.etf.fixit.managementservice.repository;
import ba.etf.fixit.managementservice.model.Radnik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface RadnikRepository extends JpaRepository<Radnik, Long> {
    List<Radnik> findByGradskaSluzbaId(Long gradskaSluzbaId);
    List<Radnik> findByAktivanTrue();
    @Query("SELECT COUNT(r) FROM Radnik r WHERE r.gradskaSluzba.id = :sluzbaId AND r.aktivan = true")
    long countAktivnihPoSluzbi(Long sluzbaId);
}
