package ba.etf.fixit.notificationservice.exception;
import java.time.LocalDateTime;
public class ApiGreska {
    private int status; private String greska; private String poruka; private LocalDateTime vrijemeGreske;
    public ApiGreska(int status, String greska, String poruka){this.status=status;this.greska=greska;this.poruka=poruka;this.vrijemeGreske=LocalDateTime.now();}
    public int getStatus(){return status;} public String getGreska(){return greska;}
    public String getPoruka(){return poruka;} public LocalDateTime getVrijemeGreske(){return vrijemeGreske;}
}
