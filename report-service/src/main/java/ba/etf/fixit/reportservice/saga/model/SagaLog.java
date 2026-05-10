package ba.etf.fixit.reportservice.saga.model;

import jakarta.persistence.*;
import jakarta.persistence.PostPersist;
import org.springframework.data.domain.Persistable;
import java.time.LocalDateTime;

@Entity
@Table(name = "saga_log")
public class SagaLog implements Persistable<String> {

    public enum SagaStatus {
        PENDING,      
        COMPLETED,    
        COMPENSATED   
    }

    @Id
    @Column(name = "saga_id", length = 100)
    private String sagaId;

    @Transient
    private boolean isNew = true;

    @Column(name = "prijava_id", nullable = false)
    private Long prijavaId;

    @Column(name = "stari_status", length = 100)
    private String stariStatus;

    @Column(name = "novi_status", length = 100)
    private String noviStatus;

    @Column(name = "korisnik_id")
    private Long korisnikId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SagaStatus status = SagaStatus.PENDING;

    @Column(name = "datum_kreiranja", nullable = false, updatable = false)
    private LocalDateTime datumKreiranja;

    @Column(name = "datum_azuriranja")
    private LocalDateTime datumAzuriranja;

    @Column(name = "razlog_kompenzacije", length = 500)
    private String razlogKompenzacije;

    @PrePersist
    protected void onCreate() {
        this.datumKreiranja = LocalDateTime.now();
        this.datumAzuriranja = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.datumAzuriranja = LocalDateTime.now();
    }

    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @PostPersist
    void markNotNewAfterPersist() {
        this.isNew = false;
    }

    @Override
    public String getId() { return sagaId; }

    @Override
    public boolean isNew() { return isNew; }

    public SagaLog() {}

    public SagaLog(String sagaId, Long prijavaId, String stariStatus, String noviStatus, Long korisnikId) {
        this.sagaId = sagaId;
        this.prijavaId = prijavaId;
        this.stariStatus = stariStatus;
        this.noviStatus = noviStatus;
        this.korisnikId = korisnikId;
        this.status = SagaStatus.PENDING;
        this.isNew = true;
    }

    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }

    public Long getPrijavaId() { return prijavaId; }
    public void setPrijavaId(Long prijavaId) { this.prijavaId = prijavaId; }

    public String getStariStatus() { return stariStatus; }
    public void setStariStatus(String stariStatus) { this.stariStatus = stariStatus; }

    public String getNoviStatus() { return noviStatus; }
    public void setNoviStatus(String noviStatus) { this.noviStatus = noviStatus; }

    public Long getKorisnikId() { return korisnikId; }
    public void setKorisnikId(Long korisnikId) { this.korisnikId = korisnikId; }

    public SagaStatus getStatus() { return status; }
    public void setStatus(SagaStatus status) { this.status = status; }

    public LocalDateTime getDatumKreiranja() { return datumKreiranja; }
    public LocalDateTime getDatumAzuriranja() { return datumAzuriranja; }

    public String getRazlogKompenzacije() { return razlogKompenzacije; }
    public void setRazlogKompenzacije(String razlogKompenzacije) { this.razlogKompenzacije = razlogKompenzacije; }
}