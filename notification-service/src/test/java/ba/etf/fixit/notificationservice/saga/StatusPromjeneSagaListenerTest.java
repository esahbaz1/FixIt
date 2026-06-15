package ba.etf.fixit.notificationservice.saga;

import ba.etf.fixit.notificationservice.dto.NotifikacijaRequestDTO;
import ba.etf.fixit.notificationservice.dto.NotifikacijaResponseDTO;
import ba.etf.fixit.notificationservice.model.TipNotifikacije;
import ba.etf.fixit.notificationservice.saga.config.RabbitMQKonfiguracija;
import ba.etf.fixit.notificationservice.saga.event.KreiranjeNotifikacijeNijeUspiloEvent;
import ba.etf.fixit.notificationservice.saga.event.NotifikacijaKreiranaEvent;
import ba.etf.fixit.notificationservice.saga.event.StatusPrijavePromijenjenEvent;
import ba.etf.fixit.notificationservice.saga.listener.StatusPromjeneSagaListener;
import ba.etf.fixit.notificationservice.service.NotifikacijaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatusPromjeneSagaListenerTest {

    @Mock private NotifikacijaService notifikacijaService;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks private StatusPromjeneSagaListener listener;

    private StatusPrijavePromijenjenEvent napraviBazicniEvent() {
        StatusPrijavePromijenjenEvent event = new StatusPrijavePromijenjenEvent();
        event.setSagaId("saga-123");
        event.setPrijavaId(100L);
        event.setKorisnikId(1L); // podnosilac
        event.setNaslovPrijave("Test Prijava");
        event.setStariStatus("Na cekanju");
        event.setNoviStatus("U radu");
        event.setOdgovornoLiceId(null);
        return event;
    }

    @Test
    void handleStatusPromijenjen_promjenaStatusa_samoPodnosilac_uspjesno() {
        // Arrange
        StatusPrijavePromijenjenEvent event = napraviBazicniEvent();
        
        NotifikacijaResponseDTO mockResponse = new NotifikacijaResponseDTO();
        mockResponse.setId(55L);
        
        when(notifikacijaService.kreiraj(any(NotifikacijaRequestDTO.class))).thenReturn(mockResponse);

        // Act
        listener.handleStatusPromijenjen(event);

        // Assert
        ArgumentCaptor<NotifikacijaRequestDTO> dtoCaptor = ArgumentCaptor.forClass(NotifikacijaRequestDTO.class);
        verify(notifikacijaService, times(1)).kreiraj(dtoCaptor.capture());
        
        NotifikacijaRequestDTO poslaniDto = dtoCaptor.getValue();
        assertEquals(event.getKorisnikId(), poslaniDto.getKorisnikId());
        assertEquals(TipNotifikacije.STATUS_PROMJENA, poslaniDto.getTip());
        assertTrue(poslaniDto.getTekst().contains("Status vase prijave"));

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQKonfiguracija.SAGA_EXCHANGE),
                eq(RabbitMQKonfiguracija.NOTIFIKACIJA_KREIRANA_ROUTING_KEY),
                any(NotifikacijaKreiranaEvent.class)
        );
    }

    @Test
    void handleStatusPromijenjen_promjenaStatusa_obavijestiIRadnika_uspjesno() {
        // Arrange
        StatusPrijavePromijenjenEvent event = napraviBazicniEvent();
        event.setOdgovornoLiceId(99L); // Radnik je dodijeljen i različit od podnosioca

        NotifikacijaResponseDTO mockResponse = new NotifikacijaResponseDTO();
        mockResponse.setId(55L);
        
        when(notifikacijaService.kreiraj(any(NotifikacijaRequestDTO.class))).thenReturn(mockResponse);

        // Act
        listener.handleStatusPromijenjen(event);

        // Assert
        ArgumentCaptor<NotifikacijaRequestDTO> dtoCaptor = ArgumentCaptor.forClass(NotifikacijaRequestDTO.class);
        verify(notifikacijaService, times(2)).kreiraj(dtoCaptor.capture());
        
        List<NotifikacijaRequestDTO> sviPozivi = dtoCaptor.getAllValues();
        assertEquals(1L, sviPozivi.get(0).getKorisnikId()); // podnosilac
        assertEquals(99L, sviPozivi.get(1).getKorisnikId()); // radnik
        assertTrue(sviPozivi.get(1).getTekst().contains("koja vam je dodijeljena"));

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQKonfiguracija.SAGA_EXCHANGE),
                eq(RabbitMQKonfiguracija.NOTIFIKACIJA_KREIRANA_ROUTING_KEY),
                any(NotifikacijaKreiranaEvent.class)
        );
    }

    @Test
    void handleStatusPromijenjen_dodjelaRadniku_uspjesno() {
        // Arrange
        StatusPrijavePromijenjenEvent event = napraviBazicniEvent();
        event.setNoviStatus("DODJELA_RADNIKU");
        event.setOdgovornoLiceId(99L);

        NotifikacijaResponseDTO mockResponse = new NotifikacijaResponseDTO();
        mockResponse.setId(66L);
        
        when(notifikacijaService.kreiraj(any(NotifikacijaRequestDTO.class))).thenReturn(mockResponse);

        // Act
        listener.handleStatusPromijenjen(event);

        // Assert
        ArgumentCaptor<NotifikacijaRequestDTO> dtoCaptor = ArgumentCaptor.forClass(NotifikacijaRequestDTO.class);
        verify(notifikacijaService, times(1)).kreiraj(dtoCaptor.capture());
        
        NotifikacijaRequestDTO poslaniDto = dtoCaptor.getValue();
        assertEquals(99L, poslaniDto.getKorisnikId());
        assertEquals(TipNotifikacije.DODJELA_RADNIKU, poslaniDto.getTip());
        assertTrue(poslaniDto.getTekst().contains("vam je dodijeljena. Molimo vas"));

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQKonfiguracija.SAGA_EXCHANGE),
                eq(RabbitMQKonfiguracija.NOTIFIKACIJA_KREIRANA_ROUTING_KEY),
                any(NotifikacijaKreiranaEvent.class)
        );
    }

    @Test
    void handleStatusPromijenjen_simuliranaGreska_saljeGreskaEventNaRabbit() {
        // Arrange
        StatusPrijavePromijenjenEvent event = napraviBazicniEvent();
        event.setNaslovPrijave("GRESKA_NA_PRIJAVI"); // Aktivira simulaciju bacanja RuntimeException-a u listeneru

        // Act
        listener.handleStatusPromijenjen(event);

        // Assert
        verify(notifikacijaService, never()).kreiraj(any());

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQKonfiguracija.SAGA_EXCHANGE),
                eq(RabbitMQKonfiguracija.NOTIFIKACIJA_GRESKA_ROUTING_KEY),
                any(KreiranjeNotifikacijeNijeUspiloEvent.class)
        );
    }
}