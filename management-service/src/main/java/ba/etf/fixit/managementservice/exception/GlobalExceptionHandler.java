package ba.etf.fixit.managementservice.exception;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidacija(MethodArgumentNotValidException ex) {
        Map<String,String> greske = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e -> greske.put(((FieldError)e).getField(), e.getDefaultMessage()));
        Map<String,Object> odgovor = new HashMap<>();
        odgovor.put("status",400); odgovor.put("greska","VALIDATION_ERROR"); odgovor.put("poruke",greske);
        return ResponseEntity.badRequest().body(odgovor);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiGreska> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiGreska(404,"NOT_FOUND",ex.getMessage()));
    }
    @ExceptionHandler(DuplikatException.class)
    public ResponseEntity<ApiGreska> handleDuplikat(DuplikatException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiGreska(409,"CONFLICT",ex.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiGreska> handleOpsta(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiGreska(500,"INTERNAL_SERVER_ERROR","Doslo je do interne greske."));
    }
}
