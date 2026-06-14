package ba.etf.fixit.notificationservice.dto;

import ba.etf.fixit.notificationservice.model.TipNotifikacije;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifikacijaResponseDTO {
    private Long id;
    private Long korisnikId;
    private Long prijavaId;
    private String naslov;
    private String tekst;
    private TipNotifikacije tip;
    private Boolean procitano;
    private Boolean emailPoslano;
    private LocalDateTime datumKreiranja;
    private LocalDateTime datumCitanja;
}
