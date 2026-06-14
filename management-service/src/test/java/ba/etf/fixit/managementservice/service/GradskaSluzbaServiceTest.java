package ba.etf.fixit.managementservice.service;

import ba.etf.fixit.managementservice.dto.GradskaSluzbaRequestDTO;
import ba.etf.fixit.managementservice.dto.GradskaSluzbaResponseDTO;
import ba.etf.fixit.managementservice.exception.DuplikatException;
import ba.etf.fixit.managementservice.exception.ResourceNotFoundException;
import ba.etf.fixit.managementservice.model.GradskaSluzba;
import ba.etf.fixit.managementservice.repository.GradskaSluzbaRepository;
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
class GradskaSluzbaServiceTest {

    @Mock private GradskaSluzbaRepository repo;
    @InjectMocks private GradskaSluzbaService service;

    @Test
    void kreiraj_uspjesno() {
        GradskaSluzbaRequestDTO dto = new GradskaSluzbaRequestDTO();
        dto.setNaziv("JKP Rad"); dto.setOpis("Čistoća"); dto.setKontaktEmail("jkp@rad.ba");

        when(repo.existsByNaziv("JKP Rad")).thenReturn(false);
        GradskaSluzba sluzba = new GradskaSluzba(null, "JKP Rad", "Čistoća", "jkp@rad.ba", null, true);
        when(repo.save(any())).thenReturn(sluzba);

        GradskaSluzbaResponseDTO result = service.kreiraj(dto);
        assertNotNull(result);
        assertEquals("JKP Rad", result.getNaziv());
        verify(repo).save(any());
    }

    @Test
    void kreiraj_duplikatNaziv_bacaException() {
        GradskaSluzbaRequestDTO dto = new GradskaSluzbaRequestDTO();
        dto.setNaziv("JKP Rad");
        when(repo.existsByNaziv("JKP Rad")).thenReturn(true);
        assertThrows(DuplikatException.class, () -> service.kreiraj(dto));
        verify(repo, never()).save(any());
    }

    @Test
    void dohvatiPoId_postoji_vrataDTO() {
        GradskaSluzba sluzba = new GradskaSluzba(null, "JKP Voda", "Voda", "voda@ba.ba", "033-100-100", true);
        when(repo.findById(1L)).thenReturn(Optional.of(sluzba));

        GradskaSluzbaResponseDTO result = service.dohvatiPoId(1L);
        assertNotNull(result);
        assertEquals("JKP Voda", result.getNaziv());
    }

    @Test
    void dohvatiPoId_nePostoji_bacaException() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.dohvatiPoId(99L));
    }

    @Test
    void dohvatiSve_vrataListu() {
        when(repo.findAll()).thenReturn(List.of(
                new GradskaSluzba(null, "JKP A", null, null, null, true),
                new GradskaSluzba(null, "JKP B", null, null, null, true)));
        List<GradskaSluzbaResponseDTO> result = service.dohvatiSve();
        assertEquals(2, result.size());
    }

    @Test
    void azuriraj_postoji_uspjesno() {
        GradskaSluzba sluzba = new GradskaSluzba(null, "Stari naziv", null, null, null, true);
        when(repo.findById(1L)).thenReturn(Optional.of(sluzba));
        when(repo.save(any())).thenReturn(sluzba);

        GradskaSluzbaRequestDTO dto = new GradskaSluzbaRequestDTO();
        dto.setNaziv("Novi naziv"); dto.setOpis("Novi opis");
        GradskaSluzbaResponseDTO result = service.azuriraj(1L, dto);
        assertNotNull(result);
        verify(repo).save(any());
    }

    @Test
    void azuriraj_nePostoji_bacaException() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.azuriraj(99L, new GradskaSluzbaRequestDTO()));
    }

    @Test
    void obrisi_postoji_uspjesno() {
        when(repo.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> service.obrisi(1L));
        verify(repo).deleteById(1L);
    }

    @Test
    void obrisi_nePostoji_bacaException() {
        when(repo.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.obrisi(99L));
    }
@Test
void mapToResponse_uspjesno() {
    GradskaSluzba sluzba = new GradskaSluzba(
            null,
            "JKP Test",
            "Opis",
            "test@jkp.ba",
            "033-111-111",
            true
    );
    sluzba.setId(5L);

    GradskaSluzbaResponseDTO dto = service.mapToResponse(sluzba);

    assertNotNull(dto);
    assertEquals(5L, dto.getId());
    assertEquals("JKP Test", dto.getNaziv());
    assertEquals("Opis", dto.getOpis());
    assertEquals("test@jkp.ba", dto.getKontaktEmail());
    assertEquals("033-111-111", dto.getKontaktTelefon());
    assertTrue(dto.getAktivan());
}





}
