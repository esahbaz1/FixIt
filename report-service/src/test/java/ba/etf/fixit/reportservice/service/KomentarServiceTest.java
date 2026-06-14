package ba.etf.fixit.reportservice.service;

import ba.etf.fixit.reportservice.dto.HistorijaResponseDTO;
import ba.etf.fixit.reportservice.dto.KomentarRequestDTO;
import ba.etf.fixit.reportservice.dto.KomentarResponseDTO;
import ba.etf.fixit.reportservice.exception.ResourceNotFoundException;
import ba.etf.fixit.reportservice.model.*;
import ba.etf.fixit.reportservice.repository.HistorijaPrijaveRepository;
import ba.etf.fixit.reportservice.repository.KomentarRepository;
import ba.etf.fixit.reportservice.repository.PrijavaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KomentarServiceTest {

    @Mock private KomentarRepository komentarRepo;
    @Mock private PrijavaRepository prijavaRepo;
    @Mock private HistorijaPrijaveRepository historijaPrijaveRepo;
    @InjectMocks private KomentarService service;

    private Prijava napraviPrijavu() {
        Kategorija kat = new Kategorija(null, "Put", "Putevi", 1L);
        kat.setId(1L);
        Statusi status = new Statusi(null, "Novo", "Opis");
        status.setId(1L);
        Prijava p = new Prijava();
        p.setNaslov("Rupa");
        p.setOpis("Opis");
        p.setLatitude(43.0);
        p.setLongitude(18.0);
        p.setAdresa("Sarajevo");
        p.setKategorija(kat);
        p.setKorisnikId(1L);
        p.setStatus(status);
        p.setId(1L);
        return p;
    }

    @Test
    void dodaj_uspjesno() {
        Prijava p = napraviPrijavu();
        when(prijavaRepo.findById(1L)).thenReturn(Optional.of(p));

        Komentar k = new Komentar(null, 2L, p, "Naslov", "Tekst komentara", false, null);
        when(komentarRepo.save(any())).thenReturn(k);

        KomentarRequestDTO dto = new KomentarRequestDTO();
        dto.setNaslov("Naslov");
        dto.setTekst("Tekst komentara");
        dto.setInteran(false);

        // korisnikId dolazi iz konteksta (token), prosljeđuje se kao parametar
        KomentarResponseDTO result = service.dodaj(1L, dto, 2L);
        assertNotNull(result);
        assertEquals("Tekst komentara", result.getTekst());
        assertEquals(2L, result.getKorisnikId());
        verify(komentarRepo).save(any());
    }

    @Test
    void dodaj_prijavaNePostoji_bacaException() {
        when(prijavaRepo.findById(99L)).thenReturn(Optional.empty());

        KomentarRequestDTO dto = new KomentarRequestDTO();
        dto.setTekst("Komentar");

        assertThrows(ResourceNotFoundException.class, () -> service.dodaj(99L, dto, 1L));
        verify(komentarRepo, never()).save(any());
    }

    @Test
    void dohvatiJavne_vracacListu() {
        Prijava p = napraviPrijavu();
        Komentar k1 = new Komentar(null, 1L, p, "N1", "Tekst 1", false, null);
        Komentar k2 = new Komentar(null, 2L, p, "N2", "Tekst 2", false, null);
        when(komentarRepo.findByPrijavaIdAndInteranFalse(1L)).thenReturn(List.of(k1, k2));

        List<KomentarResponseDTO> result = service.dohvatiJavne(1L);
        assertEquals(2, result.size());
        assertFalse(result.get(0).getInteran());
    }

    @Test
    void dohvatiJavne_praznaLista_vraćaPrazno() {
        when(komentarRepo.findByPrijavaIdAndInteranFalse(1L)).thenReturn(List.of());
        List<KomentarResponseDTO> result = service.dohvatiJavne(1L);
        assertTrue(result.isEmpty());
    }

    @Test
void dohvatiInterne_vracaListu() {
    Prijava p = napraviPrijavu();

    Komentar k1 = new Komentar(null, 1L, p, "Interni1", "Tekst1", true, null);
    Komentar k2 = new Komentar(null, 2L, p, "Interni2", "Tekst2", true, null);

    when(komentarRepo.findByPrijavaIdAndInteranTrue(1L))
            .thenReturn(List.of(k1, k2));

    List<KomentarResponseDTO> result = service.dohvatiInterne(1L);

    assertEquals(2, result.size());
    assertTrue(result.get(0).getInteran());
}

@Test
void dohvatiHistoriju_uspjesno() {
    Prijava p = napraviPrijavu();

    TipPromjene tip = new TipPromjene();
    tip.setStatus1("NOVO");
    tip.setStatus2("U_OBRADI");

    HistorijaPrijave h = new HistorijaPrijave();
    h.setId(1L);
    h.setPrijava(p);
    h.setKorisnikId(5L);
    h.setTipPromjene(tip);

    when(prijavaRepo.existsById(1L)).thenReturn(true);
    when(historijaPrijaveRepo.findByPrijavaIdOrderByDatumPromjeneAsc(1L))
            .thenReturn(List.of(h));

    List<HistorijaResponseDTO> result = service.dohvatiHistoriju(1L);

    assertEquals(1, result.size());
    assertEquals(1L, result.get(0).getPrijavaId());
    assertEquals("NOVO", result.get(0).getStatusIz());
    assertEquals("U_OBRADI", result.get(0).getStatusU());
}


@Test
void dohvatiHistoriju_prijavaNePostoji_bacaException() {
    when(prijavaRepo.existsById(99L)).thenReturn(false);

    assertThrows(
            ResourceNotFoundException.class,
            () -> service.dohvatiHistoriju(99L)
    );

    verify(historijaPrijaveRepo, never())
            .findByPrijavaIdOrderByDatumPromjeneAsc(anyLong());
}
}
