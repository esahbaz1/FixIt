package ba.etf.fixit.userservice.repository;

import ba.etf.fixit.userservice.model.InvalidisanToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface InvalidisanTokenRepository extends JpaRepository<InvalidisanToken, Long> {

    boolean existsByTokenHash(String tokenHash);

    @Modifying
    @Transactional
    @Query("DELETE FROM InvalidisanToken t WHERE t.datumIsteka < :sada")
    int obrisiIstekle(LocalDateTime sada);
}
