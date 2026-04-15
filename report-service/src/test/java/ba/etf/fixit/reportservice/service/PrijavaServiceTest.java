package ba.etf.fixit.reportservice.service;

import ba.etf.fixit.reportservice.dto.PrijavaRequestDTO;
import ba.etf.fixit.reportservice.dto.PrijavaResponseDTO;
import ba.etf.fixit.reportservice.exception.ResourceNotFoundException;
import ba.etf.fixit.reportservice.model.*;
import ba.etf.fixit.reportservice.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrijavaServiceTest {

    @Mock private PrijavaRepository prijavaRepo;
    @Mock private KategorijaRepository kategorijaRepo;
    @Mock private StatusiRepository statusiRepo;
    @InjectMocks private PrijavaService service;

    private Kategorija napraviKategoriju() {
        Kategorija k = new Kategorija("Put/cesta", "Oštećenja cesta", 1L);
        k.setId(1L);
        return k;
    }

    private Statusi napraviStatus(String naziv) {
        Statusi s = new Statusi(naziv, "Opis statusa");
        s.setId(1L);
        return s;
    }

    private Prijava napraviPrijavu(Kategorija k, Statusi s) {
        Prijava p = new Prijava("Rupa na putu", "Velika rupa", 43.85, 18.41,
                "Titova bb", k, 1L, s);
        p.setId(1L);
        return p;
    }

    @Test
    void kreiraj_uspjesno() {
        Kategorija kat = napraviKategoriju();
        Statusi status = napraviStatus("Novo");
        when(kategorijaRepo.findById(1L)).thenReturn(Optional.of(kat));
        when(statusiRepo.findByNaziv("Novo")).thenReturn(Optional.of(status));

        Prijava savedPrijava = napraviPrijavu(kat, status);
        when(prijavaRepo.save(any())).thenReturn(savedPrijava);

        PrijavaRequestDTO dto = new PrijavaRequestDTO();
        dto.setNaslov("Rupa na putu");
        dto.setOpis("Velika rupa");
        dto.setLatitude(43.85);
        dto.setLongitude(18.41);
        dto.setAdresa("Titova bb");
        dto.setKategorijaId(1L);
        dto.setKorisnikId(1L);

        PrijavaResponseDTO result = service.kreiraj(dto);
        assertNotNull(result);
        assertEquals("Rupa na putu", result.getNaslov());
        assertEquals("Novo", result.getStatusNaziv());
        verify(prijavaRepo).save(any());
    }

    @Test
    void kreiraj_kategorijaNePostoji_bacaException() {
        when(kategorijaRepo.findById(99L)).thenReturn(Optional.empty());

        PrijavaRequestDTO dto = new PrijavaRequestDTO();
        dto.setNaslov("Test");
        dto.setOpis("Test");
        dto.setLatitude(43.0);
        dto.setLongitude(18.0);
        dto.setKategorijaId(99L);
        dto.setKorisnikId(1L);

        assertThrows(ResourceNotFoundException.class, () -> service.kreiraj(dto));
        verify(prijavaRepo, never()).save(any());
    }

    @Test
    void kreiraj_statusNovNePostoji_bacaException() {
        when(kategorijaRepo.findById(1L)).thenReturn(Optional.of(napraviKategoriju()));
        when(statusiRepo.findByNaziv("Novo")).thenReturn(Optional.empty());

        PrijavaRequestDTO dto = new PrijavaRequestDTO();
        dto.setNaslov("Test");
        dto.setOpis("Test");
        dto.setLatitude(43.0);
        dto.setLongitude(18.0);
        dto.setKategorijaId(1L);
        dto.setKorisnikId(1L);

        assertThrows(ResourceNotFoundException.class, () -> service.kreiraj(dto));
    }

    @Test
    void dohvatiPoId_postoji_vraćaDTO() {
        Kategorija kat = napraviKategoriju();
        Statusi status = napraviStatus("Novo");
        Prijava p = napraviPrijavu(kat, status);
        when(prijavaRepo.findById(1L)).thenReturn(Optional.of(p));

        PrijavaResponseDTO result = service.dohvatiPoId(1L);
        assertNotNull(result);
        assertEquals("Rupa na putu", result.getNaslov());
        assertEquals(1L, result.getKorisnikId());
    }

    @Test
    void dohvatiPoId_nePostoji_bacaException() {
        when(prijavaRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.dohvatiPoId(99L));
    }

    @Test
    void dohvatiSve_vraćaListu() {
        Kategorija kat = napraviKategoriju();
        Statusi status = napraviStatus("Novo");
        when(prijavaRepo.findByArhiviranFalse()).thenReturn(List.of(
                napraviPrijavu(kat, status),
                napraviPrijavu(kat, status)));
        List<PrijavaResponseDTO> result = service.dohvatiSve();
        assertEquals(2, result.size());
    }

    @Test
    void promijeniStatus_uspjesno() {
        Kategorija kat = napraviKategoriju();
        Statusi statusNovo = napraviStatus("Novo");
        Statusi statusUToku = napraviStatus("U toku");
        statusUToku.setId(2L);

        Prijava p = napraviPrijavu(kat, statusNovo);
        when(prijavaRepo.findById(1L)).thenReturn(Optional.of(p));
        when(statusiRepo.findByNaziv("U toku")).thenReturn(Optional.of(statusUToku));
        when(prijavaRepo.save(any())).thenReturn(p);

        PrijavaResponseDTO result = service.promijeniStatus(1L, "U toku", 1L);
        assertNotNull(result);
        verify(prijavaRepo).save(any());
    }

    @Test
    void promijeniStatus_statusNePostoji_bacaException() {
        Kategorija kat = napraviKategoriju();
        Statusi status = napraviStatus("Novo");
        Prijava p = napraviPrijavu(kat, status);
        when(prijavaRepo.findById(1L)).thenReturn(Optional.of(p));
        when(statusiRepo.findByNaziv("Nepostojeci")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.promijeniStatus(1L, "Nepostojeci", 1L));
    }

    @Test
    void arhiviraj_uspjesno() {
        Kategorija kat = napraviKategoriju();
        Statusi status = napraviStatus("Rijeseno");
        Prijava p = napraviPrijavu(kat, status);
        when(prijavaRepo.findById(1L)).thenReturn(Optional.of(p));
        when(prijavaRepo.save(any())).thenReturn(p);

        assertDoesNotThrow(() -> service.arhiviraj(1L));
        assertTrue(p.getArhiviran());
        verify(prijavaRepo).save(p);
    }
}
