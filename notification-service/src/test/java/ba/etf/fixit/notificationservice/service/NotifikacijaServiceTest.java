package ba.etf.fixit.notificationservice.service;

import ba.etf.fixit.notificationservice.dto.NotifikacijaRequestDTO;
import ba.etf.fixit.notificationservice.dto.NotifikacijaResponseDTO;
import ba.etf.fixit.notificationservice.exception.ResourceNotFoundException;
import ba.etf.fixit.notificationservice.model.Notifikacija;
import ba.etf.fixit.notificationservice.model.TipNotifikacije;
import ba.etf.fixit.notificationservice.repository.NotifikacijaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotifikacijaServiceTest {

    @Mock private NotifikacijaRepository repo;
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
    }

    @Test
    void dohvatiZaKorisnika_vraćaListu() {
        when(repo.findByKorisnikIdOrderByDatumKreiranjaDesc(1L))
                .thenReturn(List.of(napraviNotifikaciju(1L), napraviNotifikaciju(1L)));
        List<NotifikacijaResponseDTO> result = service.dohvatiZaKorisnika(1L);
        assertEquals(2, result.size());
    }

    @Test
    void dohvatiNeprocitane_vraćaSamoNeprocitane() {
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
    void oznaciBrojProcitanim_vecProcitano_azurirajOpet() {
        Notifikacija n = napraviNotifikaciju(1L);
        n.setProcitano(true);
        when(repo.findById(1L)).thenReturn(Optional.of(n));
        when(repo.save(any())).thenReturn(n);

        NotifikacijaResponseDTO result = service.oznaciBrojProcitanim(1L);
        assertNotNull(result);
        assertTrue(result.getProcitano());
    }
}
