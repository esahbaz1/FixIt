package ba.etf.fixit.userservice.repository;
import ba.etf.fixit.userservice.model.Korisnik;
import ba.etf.fixit.userservice.model.UlogaKorisnika;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface KorisnikRepository extends JpaRepository<Korisnik, Long> {
    Optional<Korisnik> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Korisnik> findByUloga(UlogaKorisnika uloga);
    List<Korisnik> findByAktivanTrue();
}
