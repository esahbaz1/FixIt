package ba.etf.fixit.managementservice.repository;
import ba.etf.fixit.managementservice.model.KorisnikProfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface KorisnikProfilRepository extends JpaRepository<KorisnikProfil, Long> {
    Optional<KorisnikProfil> findByKorisnikId(Long korisnikId);
    boolean existsByKorisnikId(Long korisnikId);
}
