package ba.etf.fixit.reportservice.client;

import ba.etf.fixit.reportservice.dto.KorisnikDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceKlijentTest {

    @Mock
    private RestTemplate restTemplate;

    private UserServiceKlijent klijent;

    @BeforeEach
    void setUp() {
        klijent = new UserServiceKlijent(restTemplate);
    }

    @Test
    void validirajKorisnika_aktivanKorisnik_vrataDTO() {
        KorisnikDTO korisnik = new KorisnikDTO(1L, "Ana", "Simic", "ana@test.ba", "GRADJANIN", true);
        when(restTemplate.getForObject(contains("/api/korisnici/1"), eq(KorisnikDTO.class)))
                .thenReturn(korisnik);

        KorisnikDTO result = klijent.validirajKorisnika(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Ana", result.getIme());
        assertTrue(result.getAktivan());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(KorisnikDTO.class));
    }

    @Test
    void validirajKorisnika_neaktivan_bacaException() {
        KorisnikDTO korisnik = new KorisnikDTO(2L, "Marko", "Peric", "marko@test.ba", "GRADJANIN", false);
        when(restTemplate.getForObject(contains("/api/korisnici/2"), eq(KorisnikDTO.class)))
                .thenReturn(korisnik);

        assertThrows(UserServiceKlijent.KorisnikNijeAktivanException.class,
                () -> klijent.validirajKorisnika(2L));
    }

    @Test
    void validirajKorisnika_korisnikNePostoji_bacaException() {
        when(restTemplate.getForObject(contains("/api/korisnici/99"), eq(KorisnikDTO.class)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThrows(UserServiceKlijent.KorisnikNijePronadjenException.class,
                () -> klijent.validirajKorisnika(99L));
    }

    @Test
    void validirajKorisnika_userServiceNedostupan_vrataNull_gracefulDegradation() {
       
        when(restTemplate.getForObject(anyString(), eq(KorisnikDTO.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        KorisnikDTO result = klijent.validirajKorisnika(1L);
        assertNull(result);
    }

    @Test
    void validirajKorisnika_neocekivanaGreska_vrataNull_gracefulDegradation() {
        when(restTemplate.getForObject(anyString(), eq(KorisnikDTO.class)))
                .thenThrow(new RuntimeException("Neočekivana greška"));

        KorisnikDTO result = klijent.validirajKorisnika(1L);
        assertNull(result);
    }

    @Test
    void validirajKorisnika_userServiceVraciNull_vrataNull() {
        when(restTemplate.getForObject(anyString(), eq(KorisnikDTO.class)))
                .thenReturn(null);

        KorisnikDTO result = klijent.validirajKorisnika(1L);
        assertNull(result);
    }
}
