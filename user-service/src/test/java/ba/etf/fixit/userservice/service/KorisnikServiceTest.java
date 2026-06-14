package ba.etf.fixit.userservice.service;

import ba.etf.fixit.userservice.dto.*;
import ba.etf.fixit.userservice.exception.*;
import ba.etf.fixit.userservice.model.*;
import ba.etf.fixit.userservice.repository.KorisnikRepository;
import ba.etf.fixit.userservice.security.JwtServis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KorisnikServiceTest {

    @Mock private KorisnikRepository korisnikRepository;
    @Mock private JwtServis jwtServis;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private KorisnikService korisnikService;

    // Pomocna metoda - uloga se vise ne prima u DTO
    private RegistracijaRequestDTO napraviDto(String email) {
        RegistracijaRequestDTO dto = new RegistracijaRequestDTO();
        dto.setIme("Test");
        dto.setPrezime("Korisnik");
        dto.setEmail(email);
        dto.setLozinka("Lozinka123!");
        return dto;
    }

    @Test
    void registruj_novi_uspjesno() {
        RegistracijaRequestDTO dto = napraviDto("test@test.ba");

        when(korisnikRepository.existsByEmail("test@test.ba")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashirana");

        Korisnik k = new Korisnik(1L, "Test", "Korisnik", "test@test.ba",
                "hashirana", UlogaKorisnika.GRADJANIN, true, null);
        when(korisnikRepository.save(any())).thenReturn(k);

        KorisnikResponseDTO result = korisnikService.registruj(dto);

        assertNotNull(result);
        assertEquals("test@test.ba", result.getEmail());
        // Kljucna provjera - uloga mora biti GRADJANIN bez obzira sto smo proslijedili
        assertEquals(UlogaKorisnika.GRADJANIN, result.getUloga());
    }

    @Test
    void registruj_duplikatEmail_bacaException() {
        RegistracijaRequestDTO dto = napraviDto("postoji@test.ba");

        when(korisnikRepository.existsByEmail("postoji@test.ba")).thenReturn(true);

        assertThrows(DuplikatException.class, () -> korisnikService.registruj(dto));
        verify(korisnikRepository, never()).save(any());
    }

    @Test
    void dohvatiPoId_nePostoji_bacaException() {
        when(korisnikRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> korisnikService.dohvatiPoId(99L));
    }

    @Test
    void prijava_pogresnaLozinka_bacaException() {
        Korisnik k = new Korisnik(1L, "Test", "Test", "t@t.ba",
                "hashirana123!", UlogaKorisnika.GRADJANIN, true, null);
        when(korisnikRepository.findByEmail("t@t.ba")).thenReturn(Optional.of(k));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("t@t.ba");
        dto.setLozinka("pogresna");

        assertThrows(ResourceNotFoundException.class, () -> korisnikService.prijava(dto));
    }

    @Test
    void prijava_uspjesno_vraceTokene() {
        Korisnik k = new Korisnik(1L, "Test", "Test", "t@t.ba",
                "hashirana123!", UlogaKorisnika.GRADJANIN, true, null);
        when(korisnikRepository.findByEmail("t@t.ba")).thenReturn(Optional.of(k));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtServis.kreirajToken(k)).thenReturn("access-token");
        when(jwtServis.kreirajRefreshToken(k)).thenReturn("refresh-token");

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("t@t.ba");
        dto.setLozinka("Lozinka123!");

        LoginResponseDTO result = korisnikService.prijava(dto);

        assertNotNull(result.getToken());
        assertNotNull(result.getRefreshToken());
        assertEquals("access-token", result.getToken());
        assertEquals("refresh-token", result.getRefreshToken());
    }

    @Test
    void osvjeziToken_nevalidanRefresh_bacaException() {
        RefreshTokenRequestDTO dto = new RefreshTokenRequestDTO("nevaljan-token");
        when(jwtServis.jeValidanRefreshToken("nevaljan-token")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> korisnikService.osvjeziToken(dto));
    }

    
    @Test
    void obrisi_nePostoji_bacaException() {
        when(korisnikRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> korisnikService.obrisi(99L));
        verify(korisnikRepository, never()).deleteById(any());
    }
}