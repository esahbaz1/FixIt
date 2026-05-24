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
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    public PrijavaService(
            PrijavaRepository prijavaRepo,
            KategorijaRepository kategorijaRepo,
            StatusiRepository statusiRepo,
            TipPromjeneRepository tipPromjeneRepo,
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
        return prijavaRepo.findByArhiviranFalse().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public PrijavaResponseDTO dohvatiPoId(Long id) {
        return mapToResponse(nadji(id));
    }

    public PrijavaResponseDTO kreiraj(PrijavaRequestDTO dto) {
        userServiceKlijent.validirajKorisnika(dto.getKorisnikId());
        log.info("Kreiranje prijave za korisnikId={}", dto.getKorisnikId());

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
        p.setKorisnikId(dto.getKorisnikId());
        p.setStatus(status);
        if (dto.getPrioritet() != null) p.setPrioritet(dto.getPrioritet());
        if (dto.getDatumRoka() != null) p.setDatumRoka(dto.getDatumRoka());

        return mapToResponse(prijavaRepo.save(p));
    }

    public PrijavaResponseDTO promijeniStatus(Long id, String noviStatusNaziv, Long korisnikId) {
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

        Prijava savedPrijava = prijavaRepo.save(p);

        HistorijaPrijave h = new HistorijaPrijave();
        h.setPrijava(savedPrijava);
        h.setTipPromjene(tip);
        h.setKorisnikId(korisnikId);
        historijaPrijaveRepo.save(h);

        String sagaId = "saga-" + UUID.randomUUID().toString();
        SagaLog sagaLog = new SagaLog(sagaId, savedPrijava.getId(), stariNaziv, noviStatusNaziv, korisnikId);
        try {
            sagaLogRepository.save(sagaLog);
            log.info("[SAGA] SagaLog sacuvan: {}", sagaId);
        } catch (Exception e) {
            log.error("[SAGA] GRESKA pri cuvanju SagaLog-a: {}", e.getMessage(), e);
            throw e;
        }

        StatusPrijavePromijenjenEvent event = new StatusPrijavePromijenjenEvent(
                savedPrijava.getId(), savedPrijava.getKorisnikId(),
                stariNaziv, noviStatusNaziv, savedPrijava.getNaslov(), sagaId);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQKonfiguracija.SAGA_EXCHANGE,
                    RabbitMQKonfiguracija.STATUS_PROMIJENJEN_ROUTING_KEY,
                    event);
            log.info("[SAGA] TX1 zavrsena. sagaId={}, prijavaId={}, status: {} -> {}",
                    sagaId, savedPrijava.getId(), stariNaziv, noviStatusNaziv);
        } catch (Exception e) {
            log.error("[SAGA] GRESKA pri slanju RabbitMQ eventa: {}", e.getMessage(), e);
            throw e;
        }

        return mapToResponse(savedPrijava);
    }

    public void arhiviraj(Long id) {
        Prijava p = nadji(id);
        p.setArhiviran(true);
        prijavaRepo.save(p);
    }

    public PrijavaResponseDTO partialUpdate(Long id, Map<String, Object> fields) {
        Prijava p = prijavaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prijava nije pronadjena"));
        if (fields.containsKey("naslov")) p.setNaslov((String) fields.get("naslov"));
        if (fields.containsKey("opis")) p.setOpis((String) fields.get("opis"));
        if (fields.containsKey("adresa")) p.setAdresa((String) fields.get("adresa"));
        if (fields.containsKey("prioritet")) p.setPrioritet(PrioritetPrijave.valueOf((String) fields.get("prioritet")));
        return mapToResponse(prijavaRepo.save(p));
    }

    public PrijavaResponseDTO dodijeliSluzbu(Long prijavaId, Long sluzbaId) {
        Prijava p = nadji(prijavaId);
        p.setGrdSluzbald(sluzbaId);
        // Automatski prelaz u status "Dodijeljeno" ako je trenutno "Novo"
        if (p.getStatus() != null && "Novo".equals(p.getStatus().getNaziv())) {
            statusiRepo.findByNaziv("Dodijeljeno").ifPresent(p::setStatus);
        }
        return mapToResponse(prijavaRepo.save(p));
    }

    public PrijavaResponseDTO dodijeliRadnika(Long prijavaId, Long korisnikId) {
        Prijava p = nadji(prijavaId);
        p.setOdgovornoLiceId(korisnikId);
        return mapToResponse(prijavaRepo.save(p));
    }

    private Prijava nadji(Long id) {
        return prijavaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prijava sa ID-em " + id + " nije pronadjena"));
    }

    public List<PrijavaResponseDTO> dohvatiSvePaged(int page, int size, String sortBy) {
        PageRequest pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sortBy));
        return prijavaRepo.findByArhiviranFalse(pageable).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<PrijavaResponseDTO> hitneSaPrekoracenimRokom() {
        return prijavaRepo.findHitneSaPrekoracenimRokom(LocalDateTime.now())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
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

        // Dohvati naziv dodijeljene gradske službe
        if (p.getGrdSluzbald() != null) {
            String nazivSluzbe = managementServiceKlijent.dohvatiNazivSluzbe(p.getGrdSluzbald());
            dto.setNazivSluzbe(nazivSluzbe);
        }

        // Dohvati ime odgovornog radnika (korisnikId pohranjen kao odgovornoLiceId)
        if (p.getOdgovornoLiceId() != null) {
            String imeRadnika = managementServiceKlijent.dohvatiImeRadnika(p.getOdgovornoLiceId());
            dto.setImeRadnika(imeRadnika);
        }

        return dto;
    }
}