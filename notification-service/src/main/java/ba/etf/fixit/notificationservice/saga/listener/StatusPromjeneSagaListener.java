package ba.etf.fixit.notificationservice.saga.listener;

import ba.etf.fixit.notificationservice.dto.NotifikacijaRequestDTO;
import ba.etf.fixit.notificationservice.dto.NotifikacijaResponseDTO;
import ba.etf.fixit.notificationservice.model.TipNotifikacije;
import ba.etf.fixit.notificationservice.saga.config.RabbitMQKonfiguracija;
import ba.etf.fixit.notificationservice.saga.event.KreiranjeNotifikacijeNijeUspiloEvent;
import ba.etf.fixit.notificationservice.saga.event.NotifikacijaKreiranaEvent;
import ba.etf.fixit.notificationservice.saga.event.StatusPrijavePromijenjenEvent;
import ba.etf.fixit.notificationservice.service.NotifikacijaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Slusa RabbitMQ event StatusPromijenjen / DodjelaRadniku i kreira
 * notifikacije za sve relevantne korisnike (podnosilac + radnik).
 */
@Component
public class StatusPromjeneSagaListener {

    private static final Logger log = LoggerFactory.getLogger(StatusPromjeneSagaListener.class);

    private final NotifikacijaService notifikacijaService;
    private final RabbitTemplate rabbitTemplate;

    public StatusPromjeneSagaListener(NotifikacijaService notifikacijaService,
                                      RabbitTemplate rabbitTemplate) {
        this.notifikacijaService = notifikacijaService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQKonfiguracija.STATUS_PROMIJENJEN_QUEUE)
    public void handleStatusPromijenjen(StatusPrijavePromijenjenEvent event) {
        log.info("[SAGA] Primljen event. sagaId={}, prijavaId={}, status: {} -> {}",
                event.getSagaId(), event.getPrijavaId(), event.getStariStatus(), event.getNoviStatus());

        try {
            if (event.getNaslovPrijave() != null && event.getNaslovPrijave().contains("GRESKA")) {
                throw new RuntimeException("Simulirana greska za testiranje kompenzacije!");
            }

            boolean jeDodjela = "DODJELA_RADNIKU".equals(event.getNoviStatus());

            if (jeDodjela) {
                obradiDodjelu(event);
            } else {
                obradiStatusPromjenu(event);
            }

        } catch (Exception e) {
            log.error("[SAGA] TX2 pala! sagaId={}, greska={}", event.getSagaId(), e.getMessage());
            rabbitTemplate.convertAndSend(
                    RabbitMQKonfiguracija.SAGA_EXCHANGE,
                    RabbitMQKonfiguracija.NOTIFIKACIJA_GRESKA_ROUTING_KEY,
                    new KreiranjeNotifikacijeNijeUspiloEvent(
                            event.getPrijavaId(), event.getSagaId(), e.getMessage(), event.getStariStatus()));
        }
    }

    /**
     * Promjena statusa - obavijesti podnosioca.
     * Ako je radnik dodijeljen, obavijesti i njega o promjeni na "njegovoj" prijavi.
     */
    private void obradiStatusPromjenu(StatusPrijavePromijenjenEvent event) {
        String naslovPrijave = event.getNaslovPrijave() != null ? event.getNaslovPrijave() : "(bez naslova)";
        String stari = event.getStariStatus() != null ? event.getStariStatus() : "N/A";
        String novi  = event.getNoviStatus();
        TipNotifikacije tip = odredTip(novi);

        // Notifikacija za podnosioca prijave
        String tekstPodnosilac = String.format(
                "Status vase prijave \"%s\" je promijenjen: %s -> %s.",
                naslovPrijave, stari, novi);

        NotifikacijaResponseDTO saved = notifikacijaService.kreiraj(
                gradNotifDTO(
                        event.getKorisnikId(),
                        event.getPrijavaId(),
                        naslovNotifZaPodnosioca(tip),
                        tekstPodnosilac,
                        tip));

        log.info("[SAGA] TX2 OK - notifikacija za podnosioca id={}, sagaId={}", saved.getId(), event.getSagaId());

        // Notifikacija za radnika (ako je dodijeljen i nije ista osoba kao podnosilac)
        Long radnikId = event.getOdgovornoLiceId();
        if (radnikId != null && !radnikId.equals(event.getKorisnikId())) {
            String tekstRadnik = String.format(
                    "Status prijave \"%s\" koja vam je dodijeljena je promijenjen: %s -> %s.",
                    naslovPrijave, stari, novi);

            notifikacijaService.kreiraj(
                    gradNotifDTO(
                            radnikId,
                            event.getPrijavaId(),
                            "Promjena na prijavi koja vam je dodijeljena",
                            tekstRadnik,
                            TipNotifikacije.STATUS_PROMJENA));

            log.info("[SAGA] Notifikacija poslana radniku {}", radnikId);
        }

        rabbitTemplate.convertAndSend(
                RabbitMQKonfiguracija.SAGA_EXCHANGE,
                RabbitMQKonfiguracija.NOTIFIKACIJA_KREIRANA_ROUTING_KEY,
                new NotifikacijaKreiranaEvent(event.getPrijavaId(), saved.getId(), event.getSagaId()));
    }

    /**
     * Dodjela radnika - obavijesti radnika da mu je dodijeljena nova prijava.
     */
    private void obradiDodjelu(StatusPrijavePromijenjenEvent event) {
        String naslovPrijave = event.getNaslovPrijave() != null ? event.getNaslovPrijave() : "(bez naslova)";
        Long radnikId = event.getOdgovornoLiceId();

        if (radnikId == null) {
            log.warn("[DODJELA] odgovornoLiceId je null, preskacam. sagaId={}", event.getSagaId());
            return;
        }

        String tekst = String.format(
                "Prijava \"%s\" vam je dodijeljena. Molimo vas da je pregledate i preduzmete aktivnosti.",
                naslovPrijave);

        NotifikacijaResponseDTO saved = notifikacijaService.kreiraj(
                gradNotifDTO(
                        radnikId,
                        event.getPrijavaId(),
                        "Dodijeljena vam je nova prijava",
                        tekst,
                        TipNotifikacije.DODJELA_RADNIKU));

        log.info("[DODJELA] Notifikacija kreirana za radnika {} - id={}", radnikId, saved.getId());

        rabbitTemplate.convertAndSend(
                RabbitMQKonfiguracija.SAGA_EXCHANGE,
                RabbitMQKonfiguracija.NOTIFIKACIJA_KREIRANA_ROUTING_KEY,
                new NotifikacijaKreiranaEvent(event.getPrijavaId(), saved.getId(), event.getSagaId()));
    }

    // --- Helpers -------------------------------------------------------------

    private NotifikacijaRequestDTO gradNotifDTO(Long korisnikId, Long prijavaId,
                                                String naslov, String tekst, TipNotifikacije tip) {
        NotifikacijaRequestDTO dto = new NotifikacijaRequestDTO();
        dto.setKorisnikId(korisnikId);
        dto.setPrijavaId(prijavaId);
        dto.setNaslov(naslov);
        dto.setTekst(tekst);
        dto.setTip(tip);
        return dto;
    }

    private String naslovNotifZaPodnosioca(TipNotifikacije tip) {
        if (TipNotifikacije.RIJESENO.equals(tip)) return "Vasa prijava je rijesena";
        if (TipNotifikacije.DODJELA_SLUZBI.equals(tip)) return "Vasa prijava je dodijeljena sluzbi";
        return "Status vase prijave je promijenjen";
    }

    private TipNotifikacije odredTip(String noviStatus) {
        if (noviStatus == null) return TipNotifikacije.STATUS_PROMJENA;
        if ("Rijeseno".equals(noviStatus))    return TipNotifikacije.RIJESENO;
        if ("Dodijeljeno".equals(noviStatus)) return TipNotifikacije.DODJELA_SLUZBI;
        return TipNotifikacije.STATUS_PROMJENA;
    }
}
