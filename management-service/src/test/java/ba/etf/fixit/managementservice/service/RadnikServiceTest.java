package ba.etf.fixit.managementservice.service;

import ba.etf.fixit.managementservice.dto.RadnikRequestDTO;
import ba.etf.fixit.managementservice.dto.RadnikResponseDTO;
import ba.etf.fixit.managementservice.exception.ResourceNotFoundException;
import ba.etf.fixit.managementservice.model.GradskaSluzba;
import ba.etf.fixit.managementservice.model.Radnik;
import ba.etf.fixit.managementservice.repository.GradskaSluzbaRepository;
import ba.etf.fixit.managementservice.repository.RadnikRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import ba.etf.fixit.managementservice.client.UserServiceKlijent;
@ExtendWith(MockitoExtension.class)
class RadnikServiceTest {

    @Mock private RadnikRepository radnikRepo;
    @Mock private GradskaSluzbaRepository sluzbaRepo;
    @Mock private UserServiceKlijent userServiceKlijent;
    @InjectMocks private RadnikService service;
    private UserServiceKlijent.KorisnikInfo napraviKorisnika(Long id) {
    UserServiceKlijent.KorisnikInfo k = new UserServiceKlijent.KorisnikInfo();
    k.setId(id);
    k.setIme("Test");
    k.setPrezime("Korisnik");
    return k;
}

    private GradskaSluzba napraviSluzbu() {
        GradskaSluzba s = new GradskaSluzba(null, "JKP Test", "Opis", "jkp@test.ba", "033-000-000", true);
        s.setId(1L);
        return s;
    }

    @Test
    void kreiraj_uspjesno() {
        GradskaSluzba sluzba = napraviSluzbu();
        when(sluzbaRepo.findById(1L)).thenReturn(Optional.of(sluzba));
         when(userServiceKlijent.dohvatiKorisnika(10L))
        .thenReturn(napraviKorisnika(10L));
        Radnik radnik = new Radnik(null, 10L, sluzba, "Inspektor", "Elektrika", true);
        when(radnikRepo.save(any())).thenReturn(radnik);

        RadnikRequestDTO dto = new RadnikRequestDTO();
        dto.setKorisnikId(10L);
        dto.setGradskaSluzbaId(1L);
        dto.setPozicija("Inspektor");
        dto.setKompetencije("Elektrika");

        RadnikResponseDTO result = service.kreiraj(dto);
        assertNotNull(result);
        assertEquals(10L, result.getKorisnikId());
        assertEquals("JKP Test", result.getNazivSluzbe());
        verify(radnikRepo).save(any());
    }

    @Test
    void kreiraj_sluzbaNePostoji_bacaException() {
        when(sluzbaRepo.findById(99L)).thenReturn(Optional.empty());

        RadnikRequestDTO dto = new RadnikRequestDTO();
        dto.setKorisnikId(1L);
        dto.setGradskaSluzbaId(99L);

        assertThrows(ResourceNotFoundException.class, () -> service.kreiraj(dto));
        verify(radnikRepo, never()).save(any());
    }

    @Test
    void dohvatiPoId_postoji_vraćaDTO() {
        
        GradskaSluzba sluzba = napraviSluzbu();
        when(userServiceKlijent.dohvatiKorisnika(5L))
        .thenReturn(napraviKorisnika(5L));
        Radnik radnik = new Radnik(null, 5L, sluzba, "Vozač", null, true);
        when(radnikRepo.findById(1L)).thenReturn(Optional.of(radnik));

        RadnikResponseDTO result = service.dohvatiPoId(1L);
        assertNotNull(result);
        assertEquals(5L, result.getKorisnikId());
        assertEquals("Vozač", result.getPozicija());
    }

    @Test
    void dohvatiPoId_nePostoji_bacaException() {
        when(radnikRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.dohvatiPoId(99L));
    }

    @Test
    void dohvatiSve_vraćaListu() {
        GradskaSluzba sluzba = napraviSluzbu();
        when(radnikRepo.findAll()).thenReturn(List.of(
                new Radnik(null, 1L, sluzba, "Inspektor", null, true),
                new Radnik(null, 2L, sluzba, "Vozač", null, true)));
        List<RadnikResponseDTO> result = service.dohvatiSve();
        assertEquals(2, result.size());
    }

    @Test
    void dohvatiPoSluzbi_vraćaListu() {
        GradskaSluzba sluzba = napraviSluzbu();
        when(radnikRepo.findByGradskaSluzbaId(1L)).thenReturn(List.of(
                new Radnik(null, 3L, sluzba, "Čistač", null, true)));
        List<RadnikResponseDTO> result = service.dohvatiPoSluzbi(1L);
        assertEquals(1, result.size());
    }

    @Test
    void obrisi_postoji_uspjesno() {
        when(radnikRepo.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> service.obrisi(1L));
        verify(radnikRepo).deleteById(1L);
    }

    @Test
    void obrisi_nePostoji_bacaException() {
        when(radnikRepo.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.obrisi(99L));
    }
}
