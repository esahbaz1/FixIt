package ba.etf.fixit.reportservice.saga.listener;

import ba.etf.fixit.reportservice.exception.ResourceNotFoundException;
import ba.etf.fixit.reportservice.model.Prijava;
import ba.etf.fixit.reportservice.model.Statusi;
import ba.etf.fixit.reportservice.repository.PrijavaRepository;
import ba.etf.fixit.reportservice.repository.StatusiRepository;
import ba.etf.fixit.reportservice.saga.config.RabbitMQKonfiguracija;
import ba.etf.fixit.reportservice.saga.event.KreiranjeNotifikacijeNijeUspiloEvent;
import ba.etf.fixit.reportservice.saga.event.NotifikacijaKreiranaEvent;
import ba.etf.fixit.reportservice.saga.model.SagaLog;
import ba.etf.fixit.reportservice.saga.repository.SagaLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SagaOdgovorListener {

    private static final Logger log = LoggerFactory.getLogger(SagaOdgovorListener.class);

    private final SagaLogRepository sagaLogRepository;
    private final PrijavaRepository prijavaRepository;
    private final StatusiRepository statusiRepository;

    public SagaOdgovorListener(SagaLogRepository sagaLogRepository,
                                PrijavaRepository prijavaRepository,
                                StatusiRepository statusiRepository) {
        this.sagaLogRepository = sagaLogRepository;
        this.prijavaRepository = prijavaRepository;
        this.statusiRepository = statusiRepository;
    }

    @RabbitListener(queues = RabbitMQKonfiguracija.NOTIFIKACIJA_KREIRANA_QUEUE)
    public void handleNotifikacijaKreirana(NotifikacijaKreiranaEvent event) {
        log.info("[SAGA] Notifikacija kreirana. sagaId={}, prijavaId={}, notifikacijaId={}",
                event.getSagaId(), event.getPrijavaId(), event.getNotifikacijaId());

        sagaLogRepository.findById(event.getSagaId()).ifPresent(sagaLog -> {
            sagaLog.setStatus(SagaLog.SagaStatus.COMPLETED);
            sagaLogRepository.save(sagaLog);
            log.info("[SAGA] Saga {} oznacena kao COMPLETED.", event.getSagaId());
        });
    }

    @RabbitListener(queues = RabbitMQKonfiguracija.NOTIFIKACIJA_GRESKA_QUEUE)
    public void handleNotifikacijaGreska(KreiranjeNotifikacijeNijeUspiloEvent event) {
        log.warn("[SAGA] Kreiranje notifikacije nije uspjelo. sagaId={}, razlog={}",
                event.getSagaId(), event.getRazlogGreske());

        sagaLogRepository.findById(event.getSagaId()).ifPresent(sagaLog -> {
            try {
                // Za dodjelu radnika nema kompenzacije statusa - samo logujemo
                boolean jeDodjela = "DODJELA_RADNIKU".equals(sagaLog.getNoviStatus());
                if (jeDodjela) {
                    log.warn("[SAGA] Greska pri notifikaciji dodjele radnika - nema kompenzacije statusa. sagaId={}",
                            event.getSagaId());
                    sagaLog.setStatus(SagaLog.SagaStatus.COMPENSATED);
                    sagaLog.setRazlogKompenzacije(event.getRazlogGreske());
                    sagaLogRepository.save(sagaLog);
                    return;
                }

                // Kompenzacija za promjenu statusa - vrati stari status
                String statusNaVratiti = event.getStatusNaKojiVratiti() != null
                        ? event.getStatusNaKojiVratiti()
                        : sagaLog.getStariStatus();

                if (statusNaVratiti != null) {
                    Prijava prijava = prijavaRepository.findById(event.getPrijavaId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Prijava " + event.getPrijavaId() + " nije pronadjena za kompenzaciju"));
                    Statusi stariStatus = statusiRepository.findByNaziv(statusNaVratiti)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Status '" + statusNaVratiti + "' nije pronadjen"));
                    prijava.setStatus(stariStatus);
                    prijavaRepository.save(prijava);
                    log.warn("[SAGA] Kompenzacija: Status prijave {} vracen na '{}'.",
                            prijava.getId(), statusNaVratiti);
                }

                sagaLog.setStatus(SagaLog.SagaStatus.COMPENSATED);
                sagaLog.setRazlogKompenzacije(event.getRazlogGreske());
                sagaLogRepository.save(sagaLog);
                log.warn("[SAGA] Saga {} oznacena kao COMPENSATED.", event.getSagaId());

            } catch (Exception e) {
                log.error("[SAGA] Greska tokom kompenzacije za sagaId={}: {}", event.getSagaId(), e.getMessage());
            }
        });
    }
}
