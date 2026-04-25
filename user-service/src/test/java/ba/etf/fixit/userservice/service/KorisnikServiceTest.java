package ba.etf.fixit.userservice.service;

import ba.etf.fixit.userservice.dto.*;
import ba.etf.fixit.userservice.exception.*;
import ba.etf.fixit.userservice.model.*;
import ba.etf.fixit.userservice.repository.KorisnikRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KorisnikServiceTest {

    @Mock private KorisnikRepository korisnikRepository;
    @InjectMocks private KorisnikService korisnikService;

    @Test
    void registruj_novi_uspjesno() {
        RegistracijaRequestDTO dto = new RegistracijaRequestDTO();
        dto.setIme("Test"); dto.setPrezime("Korisnik");
        dto.setEmail("test@test.ba"); dto.setLozinka("Lozinka123!");
        dto.setUloga(UlogaKorisnika.GRADJANIN);

        when(korisnikRepository.existsByEmail("test@test.ba")).thenReturn(false);
        Korisnik k = new Korisnik(null, "Test", "Korisnik", "test@test.ba", "Lozinka123!", UlogaKorisnika.GRADJANIN, true, null);
        when(korisnikRepository.save(any())).thenReturn(k);

        KorisnikResponseDTO result = korisnikService.registruj(dto);
        assertNotNull(result);
        assertEquals("test@test.ba", result.getEmail());
    }

    @Test
    void registruj_duplikatEmail_bacaException() {
        RegistracijaRequestDTO dto = new RegistracijaRequestDTO();
        dto.setEmail("postoji@test.ba"); dto.setLozinka("Lozinka123!");
        dto.setIme("Test"); dto.setPrezime("Korisnik");
        when(korisnikRepository.existsByEmail("postoji@test.ba")).thenReturn(true);
        assertThrows(DuplikatException.class, () -> korisnikService.registruj(dto));
    }

    @Test
    void dohvatiPoId_nePostoji_bacaException() {
        when(korisnikRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> korisnikService.dohvatiPoId(99L));
    }

    @Test
    void prijava_pogresnaLozinka_bacaException() {
        Korisnik k = new Korisnik(null, "Test", "Test", "t@t.ba", "ispravna123!", UlogaKorisnika.GRADJANIN, true, null);
        when(korisnikRepository.findByEmail("t@t.ba")).thenReturn(Optional.of(k));
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("t@t.ba"); dto.setLozinka("pogresna");
        assertThrows(ResourceNotFoundException.class, () -> korisnikService.prijava(dto));
    }
}
