package ba.etf.fixit.notificationservice.exception;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String p){super(p);}
}
