package ba.etf.fixit.notificationservice.service;

import ba.etf.fixit.notificationservice.dto.*;
import ba.etf.fixit.notificationservice.exception.ResourceNotFoundException;
import ba.etf.fixit.notificationservice.model.Notifikacija;
import ba.etf.fixit.notificationservice.model.TipNotifikacije;
import ba.etf.fixit.notificationservice.repository.NotifikacijaRepository;
import ba.etf.fixit.notificationservice.socket.NotifikacijaSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotifikacijaService {

    private static final Logger log = LoggerFactory.getLogger(NotifikacijaService.class);

    private final NotifikacijaRepository repo;
    private final NotifikacijaSocketHandler socketHandler;

    public NotifikacijaService(NotifikacijaRepository repo,
                               NotifikacijaSocketHandler socketHandler) {
        this.repo = repo;
        this.socketHandler = socketHandler;
    }

    public List<NotifikacijaResponseDTO> dohvatiZaKorisnika(Long korisnikId) {
        return repo.findByKorisnikIdOrderByDatumKreiranjaDesc(korisnikId)
                .stream().map(this::map).collect(Collectors.toList());
    }

    public List<NotifikacijaResponseDTO> dohvatiNeprocitane(Long korisnikId) {
        return repo.findByKorisnikIdAndProcitanoFalse(korisnikId)
                .stream().map(this::map).collect(Collectors.toList());
    }

    public long brojNeprocitanih(Long korisnikId) {
        return repo.countByKorisnikIdAndProcitanoFalse(korisnikId);
    }

    /**
     * Kreira notifikaciju, sprema je u bazu i potom emituje real-time
     * Socket.IO event "nova-notifikacija" korisniku ako je online.
     */
    public NotifikacijaResponseDTO kreiraj(NotifikacijaRequestDTO dto) {
        Notifikacija n = new Notifikacija(
                null,
                dto.getKorisnikId(),
                dto.getPrijavaId(),
                dto.getNaslov(),
                dto.getTekst(),
                dto.getTip(),
                false, false, null, null
        );
        NotifikacijaResponseDTO saved = map(repo.save(n));

        // Real-time push - ne blokira ako korisnik nije online
        try {
            socketHandler.posaljiKorisniku(saved.getKorisnikId(), saved);
        } catch (Exception e) {
            log.warn("[SocketIO] Nije moguće poslati real-time event za notifikaciju {}: {}",
                     saved.getId(), e.getMessage());
        }

        return saved;
    }

    public NotifikacijaResponseDTO oznaciBrojProcitanim(Long id) {
        Notifikacija n = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notifikacija " + id + " nije pronadjena"));
        n.setProcitano(true);
        n.setDatumCitanja(LocalDateTime.now());
        return map(repo.save(n));
    }

    public List<NotifikacijaResponseDTO> dohvatiZaKorisnikaPaged(Long korisnikId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("datumKreiranja").descending());
        return repo.findByKorisnikId(korisnikId, pageable)
                .stream().map(this::map).collect(Collectors.toList());
    }

    public List<NotifikacijaResponseDTO> neprocitanePoTipu(Long korisnikId, TipNotifikacije tip) {
        return repo.findByKorisnikIdAndProcitanoFalseAndTip(korisnikId, tip)
                .stream().map(this::map).collect(Collectors.toList());
    }

    // --- Mapper --------------------------------------------------------------

    private NotifikacijaResponseDTO map(Notifikacija n) {
        NotifikacijaResponseDTO dto = new NotifikacijaResponseDTO();
        dto.setId(n.getId());
        dto.setKorisnikId(n.getKorisnikId());
        dto.setPrijavaId(n.getPrijavaId());
        dto.setNaslov(n.getNaslov());
        dto.setTekst(n.getTekst());
        dto.setTip(n.getTip());
        dto.setProcitano(n.getProcitano());
        dto.setEmailPoslano(n.getEmailPoslano());
        dto.setDatumKreiranja(n.getDatumKreiranja());
        dto.setDatumCitanja(n.getDatumCitanja());
        return dto;
    }
}
