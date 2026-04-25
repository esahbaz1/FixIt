package ba.etf.fixit.reportservice.exception;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String p){super(p);}
}
