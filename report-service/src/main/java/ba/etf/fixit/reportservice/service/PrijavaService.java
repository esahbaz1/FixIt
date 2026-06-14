package ba.etf.fixit.reportservice.service;

import ba.etf.fixit.reportservice.client.ManagementServiceKlijent;
import ba.etf.fixit.reportservice.client.UserServiceKlijent;
import ba.etf.fixit.reportservice.dto.*;
import ba.etf.fixit.reportservice.exception.ResourceNotFoundException;
import ba.etf.fixit.reportservice.model.*;
import ba.etf.fixit.reportservice.repository.*;
import ba.etf.fixit.reportservice.saga.config.RabbitMQKonfiguracija;
import ba.etf.fixit.reportservice.saga.event.StatusPrijavePromijenjenEvent;
import ba.etf.fixit.reportservice.saga.model.SagaLog;
import ba.etf.fixit.reportservice.saga.repository.SagaLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PrijavaService {

    private static final Logger log = LoggerFactory.getLogger(PrijavaService.class);

    private final PrijavaRepository prijavaRepo;
    private final KategorijaRepository kategorijaRepo;
    private final StatusiRepository statusiRepo;
    private final TipPromjeneRepository tipPromjeneRepo;
    private final HistorijaPrijaveRepository historijaPrijaveRepo;
    private final FotografijaRepository fotografijaRepo;
    private final UserServiceKlijent userServiceKlijent;
    private final ManagementServiceKlijent managementServiceKlijent;
    private final RabbitTemplate rabbitTemplate;
    private final SagaLogRepository sagaLogRepository;

    public PrijavaService(PrijavaRepository prijavaRepo, KategorijaRepository kategorijaRepo,
                          StatusiRepository statusiRepo, TipPromjeneRepository tipPromjeneRepo,
                          HistorijaPrijaveRepository historijaPrijaveRepo,
                          FotografijaRepository fotografijaRepo,
                          UserServiceKlijent userServiceKlijent,
                          ManagementServiceKlijent managementServiceKlijent,
                          RabbitTemplate rabbitTemplate,
                          SagaLogRepository sagaLogRepository) {
        this.prijavaRepo = prijavaRepo;
        this.kategorijaRepo = kategorijaRepo;
        this.statusiRepo = statusiRepo;
        this.tipPromjeneRepo = tipPromjeneRepo;
        this.historijaPrijaveRepo = historijaPrijaveRepo;
        this.fotografijaRepo = fotografijaRepo;
        this.userServiceKlijent = userServiceKlijent;
        this.managementServiceKlijent = managementServiceKlijent;
        this.rabbitTemplate = rabbitTemplate;
        this.sagaLogRepository = sagaLogRepository;
    }

    public List<PrijavaResponseDTO> dohvatiSve() {
        return prijavaRepo.findByArhiviranFalse().stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public PrijavaResponseDTO dohvatiPoId(Long id) {
        return mapToResponse(nadji(id));
    }

    /** Kreira prijavu i vraca 202 potvrdu. Korisnik prima notifikaciju kada je obradjena. */
    public Map<String, Object> kreirajAsync(PrijavaRequestDTO dto, Long korisnikId) {
        userServiceKlijent.validirajKorisnika(korisnikId);

        Kategorija kat = kategorijaRepo.findById(dto.getKategorijaId())
                .orElseThrow(() -> new ResourceNotFoundException("Kategorija " + dto.getKategorijaId() + " nije pronadjena"));
        Statusi status = statusiRepo.findByNaziv("Novo")
                .orElseThrow(() -> new ResourceNotFoundException("Status Novo nije pronadjen"));

        Prijava p = new Prijava();
        p.setNaslov(dto.getNaslov());
        p.setOpis(dto.getOpis());
        p.setLatitude(dto.getLatitude());
        p.setLongitude(dto.getLongitude());
        p.setAdresa(dto.getAdresa());
        p.setKategorija(kat);
        p.setKorisnikId(korisnikId);
        p.setStatus(status);
        if (dto.getPrioritet() != null) p.setPrioritet(dto.getPrioritet());
        if (dto.getDatumRoka() != null) p.setDatumRoka(dto.getDatumRoka());

        Prijava sacuvana = prijavaRepo.save(p);
        log.info("Prijava kreirana id={}", sacuvana.getId());

        return odgovorPokrenut(sacuvana.getId(), null);
    }

    /**
     * Pokrace SAGA za promjenu statusa.
     * Event nosi i odgovornoLiceId da notification-service moze obavijestiti i radnika.
     */
    public Map<String, Object> promijeniStatusAsync(Long id, String noviStatusNaziv, Long korisnikId) {
        Prijava p = nadji(id);
        Statusi stariStatus = p.getStatus();
        Statusi novi = statusiRepo.findByNaziv(noviStatusNaziv)
                .orElseThrow(() -> new ResourceNotFoundException("Status '" + noviStatusNaziv + "' nije pronadjen"));

        if (stariStatus != null && stariStatus.getNaziv().equals(noviStatusNaziv)) {
            throw new IllegalArgumentException("Status je vec postavljen na isti");
        }

        p.setStatus(novi);
        if ("Rijeseno".equals(noviStatusNaziv)) p.setDatumZavrsetka(LocalDateTime.now());

        String stariNaziv = stariStatus != null ? stariStatus.getNaziv() : null;
        TipPromjene tip = tipPromjeneRepo.findByStatus1AndStatus2(stariNaziv, noviStatusNaziv)
                .orElseGet(() -> tipPromjeneRepo.save(new TipPromjene(null, stariNaziv, noviStatusNaziv)));

        Prijava saved = prijavaRepo.save(p);

        HistorijaPrijave h = new HistorijaPrijave();
        h.setPrijava(saved);
        h.setTipPromjene(tip);
        h.setKorisnikId(korisnikId);
        historijaPrijaveRepo.save(h);

        String sagaId = "saga-" + UUID.randomUUID();
        sagaLogRepository.save(new SagaLog(sagaId, saved.getId(), stariNaziv, noviStatusNaziv, korisnikId));

        StatusPrijavePromijenjenEvent event = new StatusPrijavePromijenjenEvent(
                saved.getId(), saved.getKorisnikId(), saved.getOdgovornoLiceId(),
                stariNaziv, noviStatusNaziv, saved.getNaslov(), sagaId);

        rabbitTemplate.convertAndSend(
                RabbitMQKonfiguracija.SAGA_EXCHANGE,
                RabbitMQKonfiguracija.STATUS_PROMIJENJEN_ROUTING_KEY,
                event);

        log.info("[SAGA] sagaId={}, prijavaId={}, status: {} -> {}", sagaId, saved.getId(), stariNaziv, noviStatusNaziv);

        return odgovorPokrenut(saved.getId(), sagaId);
    }

    public void arhiviraj(Long id) {
        Prijava p = nadji(id);
        p.setArhiviran(true);
        prijavaRepo.save(p);
    }

    public PrijavaResponseDTO partialUpdate(Long id, Map<String, Object> fields) {
        Prijava p = nadji(id);
        if (fields.containsKey("naslov"))    p.setNaslov((String) fields.get("naslov"));
        if (fields.containsKey("opis"))      p.setOpis((String) fields.get("opis"));
        if (fields.containsKey("adresa"))    p.setAdresa((String) fields.get("adresa"));
        if (fields.containsKey("prioritet")) p.setPrioritet(PrioritetPrijave.valueOf((String) fields.get("prioritet")));
        return mapToResponse(prijavaRepo.save(p));
    }

    public PrijavaResponseDTO dodijeliSluzbu(Long prijavaId, Long sluzbaId) {
        Prijava p = nadji(prijavaId);
        p.setGrdSluzbald(sluzbaId);
        if (p.getStatus() != null && "Novo".equals(p.getStatus().getNaziv())) {
            statusiRepo.findByNaziv("Dodijeljeno").ifPresent(p::setStatus);
        }
        return mapToResponse(prijavaRepo.save(p));
    }

    public PrijavaResponseDTO dodijeliRadnika(Long prijavaId, Long radnikId) {
        Prijava p = nadji(prijavaId);
        p.setOdgovornoLiceId(radnikId);
        Prijava saved = prijavaRepo.save(p);

        // Obavijesti radnika putem SAGA eventa
        String sagaId = "saga-dodjela-" + UUID.randomUUID();
        sagaLogRepository.save(new SagaLog(sagaId, saved.getId(), null, "DODJELA_RADNIKU", radnikId));

        StatusPrijavePromijenjenEvent event = new StatusPrijavePromijenjenEvent(
                saved.getId(), saved.getKorisnikId(), radnikId,
                null, "DODJELA_RADNIKU", saved.getNaslov(), sagaId);

        rabbitTemplate.convertAndSend(
                RabbitMQKonfiguracija.SAGA_EXCHANGE,
                RabbitMQKonfiguracija.STATUS_PROMIJENJEN_ROUTING_KEY,
                event);

        log.info("[DODJELA] Radnik {} dodijeljen prijavi {}", radnikId, saved.getId());
        return mapToResponse(saved);
    }

    public List<PrijavaResponseDTO> dohvatiSvePaged(int page, int size, String sortBy) {
        PageRequest pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sortBy));
        return prijavaRepo.findByArhiviranFalse(pageable).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<PrijavaResponseDTO> hitneSaPrekoracenimRokom() {
        return prijavaRepo.findHitneSaPrekoracenimRokom(LocalDateTime.now())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // --- Helpers -------------------------------------------------------------

    private Prijava nadji(Long id) {
        return prijavaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prijava sa ID-em " + id + " nije pronadjena"));
    }

    private Map<String, Object> odgovorPokrenut(Long prijavaId, String sagaId) {
        Map<String, Object> r = new HashMap<>();
        r.put("prijavaId", prijavaId);
        r.put("sagaId", sagaId);
        r.put("poruka", "Akcija je pokrenuta. Obavijestit cemo vas putem notifikacija.");
        r.put("status", "POKRENUTO");
        return r;
    }

    public PrijavaResponseDTO mapToResponse(Prijava p) {
        PrijavaResponseDTO dto = new PrijavaResponseDTO();
        dto.setId(p.getId());
        dto.setNaslov(p.getNaslov());
        dto.setOpis(p.getOpis());
        dto.setLatitude(p.getLatitude());
        dto.setLongitude(p.getLongitude());
        dto.setAdresa(p.getAdresa());
        dto.setStatusNaziv(p.getStatus() != null ? p.getStatus().getNaziv() : null);
        dto.setPrioritet(p.getPrioritet());
        dto.setKategorijaId(p.getKategorija().getId());
        dto.setNazivKategorije(p.getKategorija().getNaziv());
        dto.setKorisnikId(p.getKorisnikId());
        dto.setGrdSluzbald(p.getGrdSluzbald());
        dto.setOdgovornoLiceId(p.getOdgovornoLiceId());
        dto.setDatumPodnosenja(p.getDatumPodnosenja());
        dto.setDatumRoka(p.getDatumRoka());
        dto.setDatumZavrsetka(p.getDatumZavrsetka());
        dto.setArhiviran(p.getArhiviran());

        List<String> putanje = fotografijaRepo.findByPrijavaId(p.getId())
                .stream().map(f -> f.getPutanja()).collect(Collectors.toList());
        dto.setFotografijePutanje(putanje);

        if (p.getGrdSluzbald() != null)
            dto.setNazivSluzbe(managementServiceKlijent.dohvatiNazivSluzbe(p.getGrdSluzbald()));
        if (p.getOdgovornoLiceId() != null)
            dto.setImeRadnika(managementServiceKlijent.dohvatiImeRadnika(p.getOdgovornoLiceId()));

        return dto;
    }
}
