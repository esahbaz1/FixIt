package ba.etf.fixit.notificationservice.exception;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class ApiGreska {
    private final int status; 
    private final String greska; 
    private final String poruka; 
    private final LocalDateTime vrijemeGreske;

    public ApiGreska(int status, String greska, String poruka){
        this.status=status;
        this.greska=greska;
        this.poruka=poruka;
        this.vrijemeGreske=LocalDateTime.now();
    }
}
