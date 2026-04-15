package ba.etf.fixit.userservice.exception;
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String poruka) { super(poruka); }
}
