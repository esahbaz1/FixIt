package ba.etf.fixit.reportservice.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistikaResponseDTO {
    private long ukupnoPrijava;
    private long aktivnePrijave;
    private long arhiviranePrijave;
    private long prekoraceniRokovi;
    private Map<String, Long> poKategorijama;
    private Map<String, Long> poStatusima;
    private Map<String, Long> poMjesecima;
    private Double prosjecnoVrijemeRjesavanjaH;
}
