package ba.etf.fixit.managementservice.controller;

import ba.etf.fixit.managementservice.service.ServisZaOtkrivanje;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otkrivanje")
public class KontrolerOtkrivanja {

    private final ServisZaOtkrivanje servisZaOtkrivanje;

    public KontrolerOtkrivanja(ServisZaOtkrivanje servisZaOtkrivanje) {
        this.servisZaOtkrivanje = servisZaOtkrivanje;
    }

    @GetMapping("/korisnik-instanca/direktno")
    public ResponseEntity<Map<String, Object>> direktno() {
        return ResponseEntity.ok(servisZaOtkrivanje.direktnaProvjera());
    }

    @GetMapping("/korisnik-instanca/balansirano")
    public ResponseEntity<Map<String, Object>> balansirano() {
        return ResponseEntity.ok(servisZaOtkrivanje.balansiranaProvjera());
    }
}
