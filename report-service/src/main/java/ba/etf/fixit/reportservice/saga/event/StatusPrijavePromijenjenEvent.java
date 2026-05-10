package ba.etf.fixit.reportservice.saga.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class StatusPrijavePromijenjenEvent implements Serializable {

    private Long prijavaId;
    private Long korisnikId;
    private String stariStatus;
    private String noviStatus;
    private String naslovPrijave;
    private LocalDateTime vrijemePromjene;
    private String sagaId; // korelacijski ID za praćenje sage

    public StatusPrijavePromijenjenEvent() {}

    public StatusPrijavePromijenjenEvent(Long prijavaId, Long korisnikId,
                                         String stariStatus, String noviStatus,
                                         String naslovPrijave, String sagaId) {
        this.prijavaId = prijavaId;
        this.korisnikId = korisnikId;
        this.stariStatus = stariStatus;
        this.noviStatus = noviStatus;
        this.naslovPrijave = naslovPrijave;
        this.vrijemePromjene = LocalDateTime.now();
        this.sagaId = sagaId;
    }

    public Long getPrijavaId() { return prijavaId; }
    public void setPrijavaId(Long prijavaId) { this.prijavaId = prijavaId; }

    public Long getKorisnikId() { return korisnikId; }
    public void setKorisnikId(Long korisnikId) { this.korisnikId = korisnikId; }

    public String getStariStatus() { return stariStatus; }
    public void setStariStatus(String stariStatus) { this.stariStatus = stariStatus; }

    public String getNoviStatus() { return noviStatus; }
    public void setNoviStatus(String noviStatus) { this.noviStatus = noviStatus; }

    public String getNaslovPrijave() { return naslovPrijave; }
    public void setNaslovPrijave(String naslovPrijave) { this.naslovPrijave = naslovPrijave; }

    public LocalDateTime getVrijemePromjene() { return vrijemePromjene; }
    public void setVrijemePromjene(LocalDateTime vrijemePromjene) { this.vrijemePromjene = vrijemePromjene; }

    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }
}
