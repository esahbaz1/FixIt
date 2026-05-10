package ba.etf.fixit.notificationservice.saga.listener;

import ba.etf.fixit.notificationservice.model.Notifikacija;
import ba.etf.fixit.notificationservice.model.TipNotifikacije;
import ba.etf.fixit.notificationservice.repository.NotifikacijaRepository;
import ba.etf.fixit.notificationservice.saga.config.RabbitMQKonfiguracija;
import ba.etf.fixit.notificationservice.saga.event.KreiranjeNotifikacijeNijeUspiloEvent;
import ba.etf.fixit.notificationservice.saga.event.NotifikacijaKreiranaEvent;
import ba.etf.fixit.notificationservice.saga.event.StatusPrijavePromijenjenEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StatusPromjeneSagaListener {

    private static final Logger log = LoggerFactory.getLogger(StatusPromjeneSagaListener.class);

    private final NotifikacijaRepository notifikacijaRepository;
    private final RabbitTemplate rabbitTemplate;

    public StatusPromjeneSagaListener(NotifikacijaRepository notifikacijaRepository,
                                       RabbitTemplate rabbitTemplate) {
        this.notifikacijaRepository = notifikacijaRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

  
    @RabbitListener(queues = RabbitMQKonfiguracija.STATUS_PROMIJENJEN_QUEUE)
    @Transactional
    public void handleStatusPromijenjen(StatusPrijavePromijenjenEvent event) {
        log.info("[SAGA]  Primljen event StatusPromijenjen. sagaId={}, prijavaId={}, status: {} -> {}",
                event.getSagaId(), event.getPrijavaId(), event.getStariStatus(), event.getNoviStatus());

        try {
            
            if (event.getNaslovPrijave() != null && event.getNaslovPrijave().contains("GRESKA")) {
                throw new RuntimeException("Simulirana greška u notification-service za testiranje kompenzacije!");
            }

          
            Notifikacija notifikacija = new Notifikacija();
            notifikacija.setKorisnikId(event.getKorisnikId());
            notifikacija.setPrijavaId(event.getPrijavaId());
            notifikacija.setNaslov("Status prijave promijenjen");
            notifikacija.setTekst(String.format(
                    "Status vaše prijave '%s' je promijenjen sa '%s' na '%s'.",
                    event.getNaslovPrijave(),
                    event.getStariStatus() != null ? event.getStariStatus() : "N/A",
                    event.getNoviStatus()
            ));
            notifikacija.setTip(odredTipNotifikacije(event.getNoviStatus()));
            notifikacija.setProcitano(false);
            notifikacija.setEmailPoslano(false);

            Notifikacija savedNotifikacija = notifikacijaRepository.save(notifikacija);

            log.info("[SAGA]  TX2 uspješna. Notifikacija kreirana id={}, sagaId={}",
                    savedNotifikacija.getId(), event.getSagaId());

           
            NotifikacijaKreiranaEvent odgovor = new NotifikacijaKreiranaEvent(
                    event.getPrijavaId(),
                    savedNotifikacija.getId(),
                    event.getSagaId()
            );
            rabbitTemplate.convertAndSend(
                    RabbitMQKonfiguracija.SAGA_EXCHANGE,
                    RabbitMQKonfiguracija.NOTIFIKACIJA_KREIRANA_ROUTING_KEY,
                    odgovor
            );

        } catch (Exception e) {
            log.error("[SAGA]  TX2 pala! Šaljem kompenzacijski event. sagaId={}, greška={}",
                    event.getSagaId(), e.getMessage());

            
            KreiranjeNotifikacijeNijeUspiloEvent kompenzacija = new KreiranjeNotifikacijeNijeUspiloEvent(
                    event.getPrijavaId(),
                    event.getSagaId(),
                    e.getMessage(),
                    event.getStariStatus() // status na koji se vraća
            );
            rabbitTemplate.convertAndSend(
                    RabbitMQKonfiguracija.SAGA_EXCHANGE,
                    RabbitMQKonfiguracija.NOTIFIKACIJA_GRESKA_ROUTING_KEY,
                    kompenzacija
            );
        }
    }

    private TipNotifikacije odredTipNotifikacije(String noviStatus) {
        if ("Rijeseno".equalsIgnoreCase(noviStatus)) return TipNotifikacije.RIJESENO;
        if ("U toku".equalsIgnoreCase(noviStatus)) return TipNotifikacije.STATUS_PROMJENA;
        return TipNotifikacije.STATUS_PROMJENA;
    }
}
