package ba.etf.fixit.managementservice.exception;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String p){super(p);}
}
