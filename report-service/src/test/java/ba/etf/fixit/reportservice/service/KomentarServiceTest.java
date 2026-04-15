package ba.etf.fixit.reportservice.service;

import ba.etf.fixit.reportservice.dto.KomentarRequestDTO;
import ba.etf.fixit.reportservice.dto.KomentarResponseDTO;
import ba.etf.fixit.reportservice.exception.ResourceNotFoundException;
import ba.etf.fixit.reportservice.model.*;
import ba.etf.fixit.reportservice.repository.KomentarRepository;
import ba.etf.fixit.reportservice.repository.PrijavaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KomentarServiceTest {

    @Mock private KomentarRepository komentarRepo;
    @Mock private PrijavaRepository prijavaRepo;
    @InjectMocks private KomentarService service;

    private Prijava napraviPrijavu() {
        Kategorija kat = new Kategorija("Put", "Putevi", 1L);
        kat.setId(1L);
        Statusi status = new Statusi("Novo", "Opis");
        status.setId(1L);
        Prijava p = new Prijava("Rupa", "Opis", 43.0, 18.0, "Sarajevo", kat, 1L, status);
        p.setId(1L);
        return p;
    }

    @Test
    void dodaj_uspjesno() {
        Prijava p = napraviPrijavu();
        when(prijavaRepo.findById(1L)).thenReturn(Optional.of(p));

        Komentar k = new Komentar(2L, p, "Naslov", "Tekst komentara", false);
        when(komentarRepo.save(any())).thenReturn(k);

        KomentarRequestDTO dto = new KomentarRequestDTO();
        dto.setKorisnikId(2L);
        dto.setNaslov("Naslov");
        dto.setTekst("Tekst komentara");
        dto.setInteran(false);

        KomentarResponseDTO result = service.dodaj(1L, dto);
        assertNotNull(result);
        assertEquals("Tekst komentara", result.getTekst());
        assertEquals(2L, result.getKorisnikId());
        verify(komentarRepo).save(any());
    }

    @Test
    void dodaj_prijavaNePostoji_bacaException() {
        when(prijavaRepo.findById(99L)).thenReturn(Optional.empty());

        KomentarRequestDTO dto = new KomentarRequestDTO();
        dto.setKorisnikId(1L);
        dto.setTekst("Komentar");

        assertThrows(ResourceNotFoundException.class, () -> service.dodaj(99L, dto));
        verify(komentarRepo, never()).save(any());
    }

    @Test
    void dohvatiJavne_vraćaListu() {
        Prijava p = napraviPrijavu();
        Komentar k1 = new Komentar(1L, p, "N1", "Tekst 1", false);
        Komentar k2 = new Komentar(2L, p, "N2", "Tekst 2", false);
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
}
