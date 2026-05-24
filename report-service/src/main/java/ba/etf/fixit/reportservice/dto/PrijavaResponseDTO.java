package ba.etf.fixit.reportservice.dto;

import ba.etf.fixit.reportservice.model.PrioritetPrijave;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrijavaResponseDTO {
    private Long id;
    private String naslov;
    private String opis;
    private Double latitude;
    private Double longitude;
    private String adresa;
    private String statusNaziv;
    private PrioritetPrijave prioritet;
    private Long kategorijaId;
    private String nazivKategorije;
    private Long korisnikId;
    private Long grdSluzbald;
    private Long odgovornoLiceId;
    private LocalDateTime datumPodnosenja;
    private LocalDateTime datumRoka;
    private LocalDateTime datumZavrsetka;
    private Boolean arhiviran;
    private List<String> fotografijePutanje;
    private String nazivSluzbe;      
    private String imeRadnika;       
}