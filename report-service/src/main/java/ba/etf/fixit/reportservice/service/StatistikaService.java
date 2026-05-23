package ba.etf.fixit.reportservice.service;

import ba.etf.fixit.reportservice.dto.StatistikaResponseDTO;
import ba.etf.fixit.reportservice.repository.PrijavaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class StatistikaService {

    private final PrijavaRepository prijavaRepo;

    public StatistikaService(PrijavaRepository prijavaRepo) {
        this.prijavaRepo = prijavaRepo;
    }

    public StatistikaResponseDTO dohvatiStatistiku() {
        long ukupno = prijavaRepo.count();
        long aktivne = prijavaRepo.findByArhiviranFalse().size();
        long arhivirane = prijavaRepo.findByArhiviranTrue().size();
        long prekoraceni = prijavaRepo.findPrekoraceniRokovi(LocalDateTime.now()).size();

        Map<String, Long> poKategorijama = new LinkedHashMap<>();
        for (Object[] row : prijavaRepo.countPoKategorijama()) {
            poKategorijama.put((String) row[0], (Long) row[1]);
        }

        Map<String, Long> poStatusima = new LinkedHashMap<>();
        for (Object[] row : prijavaRepo.countPoStatusima()) {
            poStatusima.put((String) row[0], (Long) row[1]);
        }

        // Posljednjih 12 mjeseci
        LocalDateTime odDatuma = LocalDateTime.now().minusMonths(12);
        Map<String, Long> poMjesecima = new LinkedHashMap<>();
        for (Object[] row : prijavaRepo.countPoMjesecima(odDatuma)) {
            poMjesecima.put((String) row[0], (Long) row[1]);
        }

        Double prosjecnoVrijeme = prijavaRepo.prosjecnoVrijemeRjesavanjaH();

        return new StatistikaResponseDTO(
                ukupno, aktivne, arhivirane, prekoraceni,
                poKategorijama, poStatusima, poMjesecima, prosjecnoVrijeme
        );
    }
}
