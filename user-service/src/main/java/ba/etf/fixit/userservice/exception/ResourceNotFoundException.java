package ba.etf.fixit.userservice.exception;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String poruka) { super(poruka); }
}
