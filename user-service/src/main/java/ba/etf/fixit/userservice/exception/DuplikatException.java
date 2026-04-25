package ba.etf.fixit.userservice.exception;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DuplikatException extends RuntimeException {
    public DuplikatException(String poruka) { super(poruka); }
}
