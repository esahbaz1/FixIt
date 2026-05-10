package ba.etf.fixit.reportservice.saga.event;

import java.io.Serializable;


public class KreiranjeNotifikacijeNijeUspiloEvent implements Serializable {

    private Long prijavaId;
    private String sagaId;
    private String razlogGreske;
    private String statusNaKojiVratiti;

    public KreiranjeNotifikacijeNijeUspiloEvent() {}

    public KreiranjeNotifikacijeNijeUspiloEvent(Long prijavaId, String sagaId,
                                                 String razlogGreske, String statusNaKojiVratiti) {
        this.prijavaId = prijavaId;
        this.sagaId = sagaId;
        this.razlogGreske = razlogGreske;
        this.statusNaKojiVratiti = statusNaKojiVratiti;
    }

    public Long getPrijavaId() { return prijavaId; }
    public void setPrijavaId(Long prijavaId) { this.prijavaId = prijavaId; }

    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }

    public String getRazlogGreske() { return razlogGreske; }
    public void setRazlogGreske(String razlogGreske) { this.razlogGreske = razlogGreske; }

    public String getStatusNaKojiVratiti() { return statusNaKojiVratiti; }
    public void setStatusNaKojiVratiti(String statusNaKojiVratiti) { this.statusNaKojiVratiti = statusNaKojiVratiti; }
}
