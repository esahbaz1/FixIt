package ba.etf.fixit.reportservice.service;

import ba.etf.fixit.reportservice.dto.ValidacijaRequestDTO;
import ba.etf.fixit.reportservice.dto.ValidacijaResponseDTO;
import ba.etf.fixit.reportservice.dto.ValidacijaStatistikaDTO;
import ba.etf.fixit.reportservice.exception.ResourceNotFoundException;
import ba.etf.fixit.reportservice.model.Prijava;
import ba.etf.fixit.reportservice.model.Validacija;
import ba.etf.fixit.reportservice.repository.PrijavaRepository;
import ba.etf.fixit.reportservice.repository.ValidacijaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ValidacijaService {

    private final ValidacijaRepository validacijaRepo;
    private final PrijavaRepository prijavaRepo;

    public ValidacijaService(ValidacijaRepository validacijaRepo, PrijavaRepository prijavaRepo) {
        this.validacijaRepo = validacijaRepo;
        this.prijavaRepo = prijavaRepo;
    }

    /**
     * Bilježi glas korisnika - korisnikId se uzima iz autentifikovanog konteksta.
     */
    public ValidacijaResponseDTO validiraj(Long prijavaId, ValidacijaRequestDTO dto, Long korisnikId) {
        Prijava prijava = prijavaRepo.findById(prijavaId)
                .orElseThrow(() -> new ResourceNotFoundException("Prijava " + prijavaId + " nije pronadjena"));

        Validacija validacija = validacijaRepo
                .findByPrijavaIdAndKorisnikId(prijavaId, korisnikId)
                .orElse(new Validacija(null, prijava, korisnikId, dto.getPotvrdjeno(), null));

        validacija.setPotvrdjeno(dto.getPotvrdjeno());
        return mapToResponse(validacijaRepo.save(validacija));
    }

    @Transactional(readOnly = true)
    public ValidacijaStatistikaDTO dohvatiStatistiku(Long prijavaId) {
        if (!prijavaRepo.existsById(prijavaId)) {
            throw new ResourceNotFoundException("Prijava " + prijavaId + " nije pronadjena");
        }
        long potvrda = validacijaRepo.countByPrijavaIdAndPotvrdjenoTrue(prijavaId);
        long osporavanja = validacijaRepo.countByPrijavaIdAndPotvrdjenoFalse(prijavaId);
        long ukupno = validacijaRepo.countByPrijavaId(prijavaId);
        return new ValidacijaStatistikaDTO(prijavaId, potvrda, osporavanja, ukupno);
    }

    private ValidacijaResponseDTO mapToResponse(Validacija v) {
        return new ValidacijaResponseDTO(
                v.getId(),
                v.getPrijava().getId(),
                v.getKorisnikId(),
                v.getPotvrdjeno(),
                v.getDatumValidacije()
        );
    }
}
