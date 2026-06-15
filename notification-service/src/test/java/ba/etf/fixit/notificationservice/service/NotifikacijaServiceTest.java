package ba.etf.fixit.notificationservice.service;

import ba.etf.fixit.notificationservice.dto.NotifikacijaRequestDTO;
import ba.etf.fixit.notificationservice.dto.NotifikacijaResponseDTO;
import ba.etf.fixit.notificationservice.exception.ResourceNotFoundException;
import ba.etf.fixit.notificationservice.model.Notifikacija;
import ba.etf.fixit.notificationservice.model.TipNotifikacije;
import ba.etf.fixit.notificationservice.repository.NotifikacijaRepository;
import ba.etf.fixit.notificationservice.socket.NotifikacijaSocketHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotifikacijaServiceTest {

    @Mock private NotifikacijaRepository repo;
    @Mock private NotifikacijaSocketHandler socketHandler;
    @InjectMocks private NotifikacijaService service;

    private Notifikacija napraviNotifikaciju(Long korisnikId) {
        Notifikacija n = new Notifikacija(null, korisnikId, 1L, "Naslov test",
                "Tekst notifikacije", TipNotifikacije.STATUS_PROMJENA, false, false, null, null);
        n.setId(1L);
        return n;
    }

    @Test
    void kreiraj_uspjesno() {
        Notifikacija n = napraviNotifikaciju(1L);
        when(repo.save(any())).thenReturn(n);

        NotifikacijaRequestDTO dto = new NotifikacijaRequestDTO();
        dto.setKorisnikId(1L);
        dto.setPrijavaId(1L);
        dto.setNaslov("Naslov test");
        dto.setTekst("Tekst notifikacije");
        dto.setTip(TipNotifikacije.STATUS_PROMJENA);

        NotifikacijaResponseDTO result = service.kreiraj(dto);
        assertNotNull(result);
        assertEquals("Naslov test", result.getNaslov());
        assertEquals(1L, result.getKorisnikId());
        assertFalse(result.getProcitano());
        verify(repo).save(any());
        verify(socketHandler).posaljiKorisniku(eq(1L), any());
    }

    @Test
    void kreiraj_greskaPriSlanjuSocketa_iDaljeSpremaUBazu() {
        Notifikacija n = napraviNotifikaciju(1L);
        when(repo.save(any())).thenReturn(n);
        doThrow(new RuntimeException("Socket error")).when(socketHandler).posaljiKorisniku(any(), any());

        NotifikacijaRequestDTO dto = new NotifikacijaRequestDTO();
        dto.setKorisnikId(1L);

        // Act & Assert (ne smije baciti izuzetak jer je unutar try-catch bloka u servisu)
        assertDoesNotThrow(() -> service.kreiraj(dto));
        verify(repo).save(any());
    }

    @Test
    void dohvatiZaKorisnika_vracaListu() {
        when(repo.findByKorisnikIdOrderByDatumKreiranjaDesc(1L))
                .thenReturn(List.of(napraviNotifikaciju(1L), napraviNotifikaciju(1L)));
        List<NotifikacijaResponseDTO> result = service.dohvatiZaKorisnika(1L);
        assertEquals(2, result.size());
    }

    @Test
    void dohvatiNeprocitane_vracaSamoNeprocitane() {
        Notifikacija n1 = napraviNotifikaciju(1L);
        n1.setProcitano(false);
        when(repo.findByKorisnikIdAndProcitanoFalse(1L)).thenReturn(List.of(n1));

        List<NotifikacijaResponseDTO> result = service.dohvatiNeprocitane(1L);
        assertEquals(1, result.size());
        assertFalse(result.get(0).getProcitano());
    }

    @Test
    void dohvatiNeprocitane_korisnikNemaNeprocitanih_praznalista() {
        when(repo.findByKorisnikIdAndProcitanoFalse(2L)).thenReturn(List.of());
        List<NotifikacijaResponseDTO> result = service.dohvatiNeprocitane(2L);
        assertTrue(result.isEmpty());
    }

    @Test
    void brojNeprocitanih_ispravniBroj() {
        when(repo.countByKorisnikIdAndProcitanoFalse(1L)).thenReturn(3L);
        assertEquals(3L, service.brojNeprocitanih(1L));
    }

    @Test
    void oznaciBrojProcitanim_uspjesno() {
        Notifikacija n = napraviNotifikaciju(1L);
        n.setProcitano(false);
        when(repo.findById(1L)).thenReturn(Optional.of(n));
        when(repo.save(any())).thenReturn(n);

        NotifikacijaResponseDTO result = service.oznaciBrojProcitanim(1L);
        assertNotNull(result);
        assertTrue(n.getProcitano());
        assertNotNull(n.getDatumCitanja());
        verify(repo).save(n);
    }

    @Test
    void oznaciBrojProcitanim_nePostoji_bacaException() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.oznaciBrojProcitanim(99L));
    }

    @Test
    void neprocitanePoTipu_vracaFiltriranuListu() {
        // Arrange
        Long korisnikId = 1L;
        TipNotifikacije tip = TipNotifikacije.NOVI_KOMENTAR;
        Notifikacija n1 = napraviNotifikaciju(korisnikId);
        n1.setTip(tip);
        
        when(repo.findByKorisnikIdAndProcitanoFalseAndTip(korisnikId, tip))
                .thenReturn(List.of(n1));

        // Act
        List<NotifikacijaResponseDTO> result = service.neprocitanePoTipu(korisnikId, tip);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TipNotifikacije.NOVI_KOMENTAR, result.get(0).getTip());
        verify(repo).findByKorisnikIdAndProcitanoFalseAndTip(korisnikId, tip);
    }

    @Test
    void neprocitanePoTipu_praznaLista_vracaPraznuListu() {
        // Arrange
        Long korisnikId = 1L;
        TipNotifikacije tip = TipNotifikacije.RIJESENO;
        
        when(repo.findByKorisnikIdAndProcitanoFalseAndTip(korisnikId, tip))
                .thenReturn(List.of());

        // Act
        List<NotifikacijaResponseDTO> result = service.neprocitanePoTipu(korisnikId, tip);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repo).findByKorisnikIdAndProcitanoFalseAndTip(korisnikId, tip);
    }

    @Test
    void dohvatiZaKorisnikaPaged_vracaPageanSadrzaj() {
        // Arrange
        Long korisnikId = 1L;
        Notifikacija n1 = napraviNotifikaciju(korisnikId);
        Notifikacija n2 = napraviNotifikaciju(korisnikId);
        
        when(repo.findByKorisnikId(eq(korisnikId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(n1, n2)));

        // Act
        List<NotifikacijaResponseDTO> result = service.dohvatiZaKorisnikaPaged(korisnikId, 0, 2);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repo).findByKorisnikId(eq(korisnikId), any(Pageable.class));
    }
}