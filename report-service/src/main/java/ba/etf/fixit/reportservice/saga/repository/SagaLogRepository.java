package ba.etf.fixit.reportservice.saga.repository;

import ba.etf.fixit.reportservice.saga.model.SagaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SagaLogRepository extends JpaRepository<SagaLog, String> {
    List<SagaLog> findByPrijavaId(Long prijavaId);
    List<SagaLog> findByStatus(SagaLog.SagaStatus status);
}
