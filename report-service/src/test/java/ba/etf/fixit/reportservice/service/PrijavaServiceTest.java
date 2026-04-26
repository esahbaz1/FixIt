package ba.etf.fixit.reportservice.service;

import ba.etf.fixit.reportservice.client.UserServiceKlijent;
import ba.etf.fixit.reportservice.dto.KorisnikDTO;
import ba.etf.fixit.reportservice.dto.PrijavaRequestDTO;
import ba.etf.fixit.reportservice.dto.PrijavaResponseDTO;
import ba.etf.fixit.reportservice.exception.ResourceNotFoundException;
import ba.etf.fixit.reportservice.model.*;
import ba.etf.fixit.reportservice.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    void kreiraj_aktivanKorisnik_uspjesno() {
        KorisnikDTO korisnik = new KorisnikDTO(1L, "Ana", "Simic", "ana@test.ba", "GRADJANIN", true);
        when(userServiceKlijent.validirajKorisnika(1L)).thenReturn(korisnik);

        Kategorija kat = napraviKategoriju();
        Statusi status = napraviStatus("Novo");
        when(kategorijaRepo.findById(1L)).thenReturn(Optional.of(kat));
        when(statusiRepo.findByNaziv("Novo")).thenReturn(Optional.of(status));
        when(prijavaRepo.save(any())).thenReturn(napraviPrijavu(kat, status));

        PrijavaRequestDTO dto = new PrijavaRequestDTO();
        dto.setNaslov("Rupa na putu"); dto.setOpis("Velika rupa");
        dto.setLatitude(43.85); dto.setLongitude(18.41);
        dto.setKategorijaId(1L); dto.setKorisnikId(1L);

        PrijavaResponseDTO result = service.kreiraj(dto);

        assertNotNull(result);
        assertEquals("Rupa na putu", result.getNaslov());
        verify(userServiceKlijent).validirajKorisnika(1L);
        verify(prijavaRepo).save(any());
    }

    @Test
    void kreiraj_korisnikNijePronadjen_bacaException() {
        when(userServiceKlijent.validirajKorisnika(99L))
                .thenThrow(new UserServiceKlijent.KorisnikNijePronadjenException("Korisnik 99 nije pronadjen"));

        PrijavaRequestDTO dto = new PrijavaRequestDTO();
        dto.setNaslov("Test"); dto.setOpis("Test opis");
        dto.setLatitude(43.0); dto.setLongitude(18.0);
        dto.setKategorijaId(1L); dto.setKorisnikId(99L);

        assertThrows(UserServiceKlijent.KorisnikNijePronadjenException.class,
                () -> service.kreiraj(dto));
        verify(prijavaRepo, never()).save(any());
    }

    @Test
    void kreiraj_korisnikNijeAktivan_bacaException() {
        when(userServiceKlijent.validirajKorisnika(2L))
                .thenThrow(new UserServiceKlijent.KorisnikNijeAktivanException("Korisnik 2 je deaktiviran"));

        PrijavaRequestDTO dto = new PrijavaRequestDTO();
        dto.setNaslov("Test"); dto.setOpis("Test opis");
        dto.setLatitude(43.0); dto.setLongitude(18.0);
        dto.setKategorijaId(1L); dto.setKorisnikId(2L);

        assertThrows(UserServiceKlijent.KorisnikNijeAktivanException.class,
                () -> service.kreiraj(dto));
        verify(prijavaRepo, never()).save(any());
    }

    @Test
    void kreiraj_userServiceNedostupan_gracefulDegradation_prijavaSeKreira() {

        when(userServiceKlijent.validirajKorisnika(1L)).thenReturn(null);

        Kategorija kat = napraviKategoriju();
        Statusi status = napraviStatus("Novo");
        when(kategorijaRepo.findById(1L)).thenReturn(Optional.of(kat));
        when(statusiRepo.findByNaziv("Novo")).thenReturn(Optional.of(status));
        when(prijavaRepo.save(any())).thenReturn(napraviPrijavu(kat, status));

        PrijavaRequestDTO dto = new PrijavaRequestDTO();
        dto.setNaslov("Test degradacija"); dto.setOpis("Test opis");
        dto.setLatitude(43.0); dto.setLongitude(18.0);
        dto.setKategorijaId(1L); dto.setKorisnikId(1L);

        PrijavaResponseDTO result = service.kreiraj(dto);
        assertNotNull(result);
        verify(prijavaRepo).save(any());
    }

    @Test
    void kreiraj_kategorijaNePostoji_bacaException() {
        when(userServiceKlijent.validirajKorisnika(1L))
                .thenReturn(new KorisnikDTO(1L, "Ana", "Simic", "ana@test.ba", "GRADJANIN", true));
        when(kategorijaRepo.findById(999L)).thenReturn(Optional.empty());

        PrijavaRequestDTO dto = new PrijavaRequestDTO();
        dto.setNaslov("Test"); dto.setOpis("Test opis");
        dto.setLatitude(43.0); dto.setLongitude(18.0);
        dto.setKategorijaId(999L); dto.setKorisnikId(1L);

        assertThrows(ResourceNotFoundException.class, () -> service.kreiraj(dto));
    }

    @Test
    void dohvatiPoId_nePostoji_bacaException() {
        when(prijavaRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.dohvatiPoId(99L));
    }
}
