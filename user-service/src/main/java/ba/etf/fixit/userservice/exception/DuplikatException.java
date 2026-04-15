package ba.etf.fixit.userservice.exception;
public class DuplikatException extends RuntimeException {
    public DuplikatException(String poruka) { super(poruka); }
}
