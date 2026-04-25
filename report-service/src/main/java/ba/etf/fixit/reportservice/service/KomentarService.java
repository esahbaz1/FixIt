package ba.etf.fixit.reportservice.service;
import ba.etf.fixit.reportservice.dto.*;
import ba.etf.fixit.reportservice.exception.ResourceNotFoundException;
import ba.etf.fixit.reportservice.model.*;
import ba.etf.fixit.reportservice.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class KomentarService {
    private final KomentarRepository komentarRepo;
    private final PrijavaRepository prijavaRepo;
    public KomentarService(KomentarRepository komentarRepo, PrijavaRepository prijavaRepo){
        this.komentarRepo=komentarRepo; this.prijavaRepo=prijavaRepo;
    }
    public List<KomentarResponseDTO> dohvatiJavne(Long prijavaId){
        return komentarRepo.findByPrijavaIdAndInteranFalse(prijavaId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    public KomentarResponseDTO dodaj(Long prijavaId, KomentarRequestDTO dto){
        Prijava p = prijavaRepo.findById(prijavaId).orElseThrow(()->new ResourceNotFoundException("Prijava "+prijavaId+" nije pronadjena"));
        Komentar k = new Komentar(null, dto.getKorisnikId(), p, dto.getNaslov(), dto.getTekst(), dto.getInteran(), null);
        return mapToResponse(komentarRepo.save(k));
    }
    private KomentarResponseDTO mapToResponse(Komentar k){
        KomentarResponseDTO dto = new KomentarResponseDTO();
        dto.setId(k.getId()); dto.setPrijavaId(k.getPrijava().getId());
        dto.setKorisnikId(k.getKorisnikId()); dto.setNaslov(k.getNaslov());
        dto.setTekst(k.getTekst()); dto.setInteran(k.getInteran()); dto.setDatumKreiranja(k.getDatumKreiranja());
        return dto;
    }
}
