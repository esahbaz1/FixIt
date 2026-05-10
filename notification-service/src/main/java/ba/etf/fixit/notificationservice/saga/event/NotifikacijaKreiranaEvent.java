package ba.etf.fixit.notificationservice.saga.event;

import java.io.Serializable;


public class NotifikacijaKreiranaEvent implements Serializable {

    private Long prijavaId;
    private Long notifikacijaId;
    private String sagaId;

    public NotifikacijaKreiranaEvent() {}

    public NotifikacijaKreiranaEvent(Long prijavaId, Long notifikacijaId, String sagaId) {
        this.prijavaId = prijavaId;
        this.notifikacijaId = notifikacijaId;
        this.sagaId = sagaId;
    }

    public Long getPrijavaId() { return prijavaId; }
    public void setPrijavaId(Long prijavaId) { this.prijavaId = prijavaId; }

    public Long getNotifikacijaId() { return notifikacijaId; }
    public void setNotifikacijaId(Long notifikacijaId) { this.notifikacijaId = notifikacijaId; }

    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }
}
