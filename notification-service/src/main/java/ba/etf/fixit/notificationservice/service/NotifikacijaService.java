package ba.etf.fixit.notificationservice.service;
import ba.etf.fixit.notificationservice.dto.*;
import ba.etf.fixit.notificationservice.exception.ResourceNotFoundException;
import ba.etf.fixit.notificationservice.model.Notifikacija;
import ba.etf.fixit.notificationservice.repository.NotifikacijaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotifikacijaService {
    private final NotifikacijaRepository repo;
    public NotifikacijaService(NotifikacijaRepository repo){this.repo=repo;}

    public List<NotifikacijaResponseDTO> dohvatiZaKorisnika(Long korisnikId){
        return repo.findByKorisnikIdOrderByDatumKreiranjaDesc(korisnikId).stream().map(this::map).collect(Collectors.toList());
    }
    public List<NotifikacijaResponseDTO> dohvatiNeprocitane(Long korisnikId){
        return repo.findByKorisnikIdAndProcitanoFalse(korisnikId).stream().map(this::map).collect(Collectors.toList());
    }
    public long brojNeprocitanih(Long korisnikId){
        return repo.countByKorisnikIdAndProcitanoFalse(korisnikId);
    }
    public NotifikacijaResponseDTO kreiraj(NotifikacijaRequestDTO dto){
        Notifikacija n = new Notifikacija(dto.getKorisnikId(),dto.getPrijavaId(),dto.getNaslov(),dto.getTekst(),dto.getTip());
        return map(repo.save(n));
    }
    public NotifikacijaResponseDTO oznaciBrojProcitanim(Long id){
        Notifikacija n = repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Notifikacija "+id+" nije pronadjena"));
        n.setProcitano(true); n.setDatumCitanja(LocalDateTime.now());
        return map(repo.save(n));
    }
    private NotifikacijaResponseDTO map(Notifikacija n){
        NotifikacijaResponseDTO dto = new NotifikacijaResponseDTO();
        dto.setId(n.getId()); dto.setKorisnikId(n.getKorisnikId()); dto.setPrijavaId(n.getPrijavaId());
        dto.setNaslov(n.getNaslov()); dto.setTekst(n.getTekst()); dto.setTip(n.getTip());
        dto.setProcitano(n.getProcitano()); dto.setEmailPoslano(n.getEmailPoslano());
        dto.setDatumKreiranja(n.getDatumKreiranja()); dto.setDatumCitanja(n.getDatumCitanja());
        return dto;
    }
}
