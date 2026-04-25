package ba.etf.fixit.managementservice.exception;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DuplikatException extends RuntimeException {
    public DuplikatException(String p){super(p);}
}
