package ba.etf.fixit.reportservice.service;

import ba.etf.fixit.reportservice.client.UserServiceKlijent;
import ba.etf.fixit.reportservice.dto.KorisnikDTO;
import ba.etf.fixit.reportservice.dto.PrijavaRequestDTO;
import ba.etf.fixit.reportservice.exception.ResourceNotFoundException;
import ba.etf.fixit.reportservice.model.*;
import ba.etf.fixit.reportservice.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrijavaServiceTest {

    @Mock private PrijavaRepository prijavaRepo;
    @Mock private KategorijaRepository kategorijaRepo;
    @Mock private StatusiRepository statusiRepo;
    @Mock private TipPromjeneRepository tipPromjeneRepo;
    @Mock private UserServiceKlijent userServiceKlijent;

    @InjectMocks private PrijavaService service;

    private Kategorija napraviKategoriju() {
        Kategorija k = new Kategorija(null, "Put/cesta", "Ostecenja cesta", 1L);
        k.setId(1L);
        return k;
    }

    private Statusi napraviStatus(String naziv) {
        Statusi s = new Statusi(null, naziv, "Opis statusa");
        s.setId(1L);
        return s;
    }

    private Prijava napraviPrijavu(Kategorija k, Statusi s) {
        Prijava p = new Prijava();
        p.setNaslov("Rupa na putu");
        p.setOpis("Velika rupa");
        p.setLatitude(43.85);
        p.setLongitude(18.41);
        p.setAdresa("Titova bb");
        p.setKategorija(k);
        p.setKorisnikId(1L);
        p.setStatus(s);
        p.setId(1L);
        return p;
    }

    private PrijavaRequestDTO napraviDto() {
        PrijavaRequestDTO dto = new PrijavaRequestDTO();
        dto.setNaslov("Rupa na putu");
        dto.setOpis("Velika rupa");
        dto.setLatitude(43.85);
        dto.setLongitude(18.41);
        dto.setKategorijaId(1L);
        return dto;
    }

    @Test
    void kreirajAsync_aktivanKorisnik_uspjesno() {
        KorisnikDTO korisnik = new KorisnikDTO(1L, "Ana", "Simic", "ana@test.ba", "GRADJANIN", true);
        when(userServiceKlijent.validirajKorisnika(1L)).thenReturn(korisnik);

        Kategorija kat = napraviKategoriju();
        Statusi status = napraviStatus("Novo");
        when(kategorijaRepo.findById(1L)).thenReturn(Optional.of(kat));
        when(statusiRepo.findByNaziv("Novo")).thenReturn(Optional.of(status));
        when(prijavaRepo.save(any())).thenReturn(napraviPrijavu(kat, status));

        Map<String, Object> result = service.kreirajAsync(napraviDto(), 1L);

        assertNotNull(result);
        assertEquals(1L, result.get("prijavaId"));
        assertEquals("POKRENUTO", result.get("status"));
        verify(userServiceKlijent).validirajKorisnika(1L);
        verify(prijavaRepo).save(any());
    }

    @Test
    void kreirajAsync_korisnikNijePronadjen_bacaException() {
        when(userServiceKlijent.validirajKorisnika(99L))
                .thenThrow(new UserServiceKlijent.KorisnikNijePronadjenException("Korisnik 99 nije pronadjen"));

        assertThrows(UserServiceKlijent.KorisnikNijePronadjenException.class,
                () -> service.kreirajAsync(napraviDto(), 99L));
        verify(prijavaRepo, never()).save(any());
    }

    @Test
    void kreirajAsync_korisnikNijeAktivan_bacaException() {
        when(userServiceKlijent.validirajKorisnika(2L))
                .thenThrow(new UserServiceKlijent.KorisnikNijeAktivanException("Korisnik 2 je deaktiviran"));

        assertThrows(UserServiceKlijent.KorisnikNijeAktivanException.class,
                () -> service.kreirajAsync(napraviDto(), 2L));
        verify(prijavaRepo, never()).save(any());
    }

    @Test
    void kreirajAsync_userServiceNedostupan_gracefulDegradation_prijavaSeKreira() {
        when(userServiceKlijent.validirajKorisnika(1L)).thenReturn(null);

        Kategorija kat = napraviKategoriju();
        Statusi status = napraviStatus("Novo");
        when(kategorijaRepo.findById(1L)).thenReturn(Optional.of(kat));
        when(statusiRepo.findByNaziv("Novo")).thenReturn(Optional.of(status));
        when(prijavaRepo.save(any())).thenReturn(napraviPrijavu(kat, status));

        PrijavaRequestDTO dto = napraviDto();
        dto.setNaslov("Test degradacija");

        Map<String, Object> result = service.kreirajAsync(dto, 1L);
        assertNotNull(result);
        assertEquals("POKRENUTO", result.get("status"));
        verify(prijavaRepo).save(any());
    }

    @Test
    void kreirajAsync_kategorijaNePostoji_bacaException() {
        when(userServiceKlijent.validirajKorisnika(1L))
                .thenReturn(new KorisnikDTO(1L, "Ana", "Simic", "ana@test.ba", "GRADJANIN", true));
        when(kategorijaRepo.findById(999L)).thenReturn(Optional.empty());

        PrijavaRequestDTO dto = napraviDto();
        dto.setKategorijaId(999L);

        assertThrows(ResourceNotFoundException.class, () -> service.kreirajAsync(dto, 1L));
    }

    @Test
    void dohvatiPoId_nePostoji_bacaException() {
        when(prijavaRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.dohvatiPoId(99L));
    }
}
