package ba.etf.fixit.userservice.integration;

import ba.etf.fixit.userservice.dto.LoginRequestDTO;
import ba.etf.fixit.userservice.dto.LoginResponseDTO;
import ba.etf.fixit.userservice.dto.OdjavaRequestDTO;
import ba.etf.fixit.userservice.dto.RefreshTokenRequestDTO;
import ba.etf.fixit.userservice.dto.RegistracijaRequestDTO;
import ba.etf.fixit.userservice.repository.InvalidisanTokenRepository;
import ba.etf.fixit.userservice.repository.KorisnikRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integracijski testovi koji pokrivaju kompletne tokove (end-to-end) kroz
 * stvarni Spring kontekst, bazu (H2) i sigurnosne filtere:
 *  - registracija -> prijava -> osvjezavanje tokena -> odjava
 *  - blacklista tokena nakon odjave
 *  - promjena uloge korisnika (admin) i provjera prava pristupa
 *  - zastita gateway-a (X-Gateway-Secret)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AutentikacijaIntegracijskiTest {

    private static final String GATEWAY_SECRET = "local-dev-secret";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private KorisnikRepository korisnikRepository;
    @Autowired private InvalidisanTokenRepository invalidisanTokenRepository;

    @BeforeEach
    void setUp() {
        invalidisanTokenRepository.deleteAll();
        korisnikRepository.deleteAll();
    }

    private RegistracijaRequestDTO validRegistracija(String email) {
        RegistracijaRequestDTO dto = new RegistracijaRequestDTO();
        dto.setIme("Emina");
        dto.setPrezime("Test");
        dto.setEmail(email);
        dto.setLozinka("Lozinka123!");
        return dto;
    }

    private LoginResponseDTO registrujIPrijavi(String email) throws Exception {
        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .content(objectMapper.writeValueAsString(validRegistracija(email))))
                .andExpect(status().isCreated());

        LoginRequestDTO login = new LoginRequestDTO();
        login.setEmail(email);
        login.setLozinka("Lozinka123!");

        String response = mockMvc.perform(post("/api/auth/prijava")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, LoginResponseDTO.class);
    }

    // --- KOMPLETAN ZIVOTNI CIKLUS TOKENA -------------------------------------

    @Test
    void kompletanTok_registracijaPrijavaRefreshOdjava() throws Exception {
        LoginResponseDTO loginResponse = registrujIPrijavi("ciklus@test.ba");

        // 1. Osvjezavanje access tokena pomocu refresh tokena
        RefreshTokenRequestDTO refreshDto = new RefreshTokenRequestDTO();
        refreshDto.setRefreshToken(loginResponse.getRefreshToken());

        String refreshResponse = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .content(objectMapper.writeValueAsString(refreshDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.poruka").value("Token osvjezen"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").value(loginResponse.getRefreshToken()))
                .andReturn().getResponse().getContentAsString();

        LoginResponseDTO refreshed = objectMapper.readValue(refreshResponse, LoginResponseDTO.class);
        assertThat(refreshed.getToken()).isNotBlank();

        // 2. Odjava - invalidise i refresh i access token
        OdjavaRequestDTO odjavaDto = new OdjavaRequestDTO();
        odjavaDto.setRefreshToken(loginResponse.getRefreshToken());
        odjavaDto.setAccessToken(refreshed.getToken());

        mockMvc.perform(post("/api/auth/odjava")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .content(objectMapper.writeValueAsString(odjavaDto)))
                .andExpect(status().isNoContent());

        // 3. Refresh token vise nije valjan nakon odjave
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .content(objectMapper.writeValueAsString(refreshDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void odjava_invalidiseRefreshTokenUBazi() throws Exception {
        LoginResponseDTO loginResponse = registrujIPrijavi("blacklist@test.ba");

        assertThat(invalidisanTokenRepository.count()).isZero();

        OdjavaRequestDTO odjavaDto = new OdjavaRequestDTO();
        odjavaDto.setRefreshToken(loginResponse.getRefreshToken());
        odjavaDto.setAccessToken(loginResponse.getToken());

        mockMvc.perform(post("/api/auth/odjava")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .content(objectMapper.writeValueAsString(odjavaDto)))
                .andExpect(status().isNoContent());

        // I refresh i access token trebaju biti u tabeli blackliste
        assertThat(invalidisanTokenRepository.count()).isEqualTo(2);
    }

    @Test
    void refresh_nevalidanToken_vraca404() throws Exception {
        RefreshTokenRequestDTO dto = new RefreshTokenRequestDTO();
        dto.setRefreshToken("ovo-nije-jwt-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void refresh_praznoTijelo_vraca400() throws Exception {
        RefreshTokenRequestDTO dto = new RefreshTokenRequestDTO();
        dto.setRefreshToken("");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greska").value("VALIDATION_ERROR"));
    }

    // --- PROMJENA ULOGE -------------------------------------------------------

    @Test
    void promijeniUlogu_kaoAdmin_uspjesno() throws Exception {
        LoginResponseDTO korisnik = registrujIPrijavi("promjena@test.ba");

        mockMvc.perform(put("/api/korisnici/" + korisnik.getId() + "/uloga")
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .header("X-Korisnik-Uloga", "ADMIN")
                        .param("novaUloga", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uloga").value("ADMIN"));

        mockMvc.perform(get("/api/korisnici/" + korisnik.getId())
                        .header("X-Gateway-Secret", GATEWAY_SECRET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uloga").value("ADMIN"));
    }

    @Test
    void promijeniUlogu_kaoGradjanin_vraca403() throws Exception {
        LoginResponseDTO korisnik = registrujIPrijavi("promjena2@test.ba");

        mockMvc.perform(put("/api/korisnici/" + korisnik.getId() + "/uloga")
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .header("X-Korisnik-Uloga", "GRADJANIN")
                        .param("novaUloga", "ADMIN"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.greska").value("FORBIDDEN"));
    }

    @Test
    void promijeniUlogu_bezUloge_vraca404() throws Exception {
        LoginResponseDTO korisnik = registrujIPrijavi("promjena3@test.ba");

        mockMvc.perform(put("/api/korisnici/" + korisnik.getId() + "/uloga")
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .param("novaUloga", "ADMIN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void promijeniUlogu_nevalidnaUloga_vraca500() throws Exception {
        LoginResponseDTO korisnik = registrujIPrijavi("promjena4@test.ba");

        mockMvc.perform(put("/api/korisnici/" + korisnik.getId() + "/uloga")
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .header("X-Korisnik-Uloga", "ADMIN")
                        .param("novaUloga", "NEPOSTOJECA"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.greska").value("INTERNAL_SERVER_ERROR"));
    }

    // --- GATEWAY SIGURNOSNI FILTER ---------------------------------------------

    @Test
    void zahtjevBezGatewaySecreta_vraca403() throws Exception {
        mockMvc.perform(get("/api/korisnici"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.greska").value("FORBIDDEN"));
    }

    @Test
    void zahtjevSaPogresnimGatewaySecretom_vraca403() throws Exception {
        mockMvc.perform(get("/api/korisnici")
                        .header("X-Gateway-Secret", "pogresan-secret"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.greska").value("FORBIDDEN"));
    }

    @Test
    void registracija_bezGatewaySecreta_vraca403() throws Exception {
        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistracija("nogw@test.ba"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.greska").value("FORBIDDEN"));
    }

    // --- DEAKTIVIRAN KORISNIK ----------------------------------------------------

    @Test
    void deaktiviranKorisnik_neMozePrijaviti() throws Exception {
        LoginResponseDTO korisnik = registrujIPrijavi("deaktivan@test.ba");

        
        var entitet = korisnikRepository.findById(korisnik.getId()).orElseThrow();
        entitet.setAktivan(false);
        korisnikRepository.save(entitet);

        LoginRequestDTO login = new LoginRequestDTO();
        login.setEmail("deaktivan@test.ba");
        login.setLozinka("Lozinka123!");

        mockMvc.perform(post("/api/auth/prijava")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void obrisaniKorisnik_brisanjeJePersistirano() throws Exception {
        LoginResponseDTO korisnik = registrujIPrijavi("persist@test.ba");

        mockMvc.perform(delete("/api/korisnici/" + korisnik.getId())
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .header("X-Korisnik-Uloga", "ADMIN"))
                .andExpect(status().isNoContent());

        assertThat(korisnikRepository.existsById(korisnik.getId())).isFalse();
    }
}