package ba.etf.fixit.userservice.controller;

import ba.etf.fixit.userservice.dto.LoginRequestDTO;
import ba.etf.fixit.userservice.dto.RegistracijaRequestDTO;
import ba.etf.fixit.userservice.model.UlogaKorisnika;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class KorisnikControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private KorisnikRepository korisnikRepository;

    @BeforeEach
    void setUp() { korisnikRepository.deleteAll(); }

    // --- USPJESNI ZAHTJEVI ---

    @Test
    void registracija_uspjesno() throws Exception {
        RegistracijaRequestDTO dto = new RegistracijaRequestDTO();
        dto.setIme("Ana"); dto.setPrezime("Simic");
        dto.setEmail("ana@test.ba"); dto.setLozinka("Lozinka123!");
        dto.setUloga(UlogaKorisnika.GRADJANIN);

        mockMvc.perform(post("/api/auth/registracija")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@test.ba"))
                .andExpect(jsonPath("$.lozinka").doesNotExist());
    }

    @Test
    void prijava_uspjesno() throws Exception {
        // Registruj korisnika
        RegistracijaRequestDTO reg = new RegistracijaRequestDTO();
        reg.setIme("Ana"); reg.setPrezime("Simic");
        reg.setEmail("ana2@test.ba"); reg.setLozinka("Lozinka123!");
        reg.setUloga(UlogaKorisnika.GRADJANIN);
        mockMvc.perform(post("/api/auth/registracija")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        // Prijavi se
        LoginRequestDTO login = new LoginRequestDTO();
        login.setEmail("ana2@test.ba"); login.setLozinka("Lozinka123!");

        mockMvc.perform(post("/api/auth/prijava")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.poruka").value("Prijava uspjesna"));
    }

    @Test
    void dohvatiSve_uspjesno() throws Exception {
        mockMvc.perform(get("/api/korisnici"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // --- NEUSPJESNI ZAHTJEVI ---

    @Test
    void registracija_nevalidanEmail_vraca400() throws Exception {
        RegistracijaRequestDTO dto = new RegistracijaRequestDTO();
        dto.setIme("Ana"); dto.setPrezime("Simic");
        dto.setEmail("NIJE_EMAIL"); dto.setLozinka("Lozinka123!");

        mockMvc.perform(post("/api/auth/registracija")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greska").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.poruke.email").exists());
    }

    @Test
    void registracija_kratkaLozinka_vraca400() throws Exception {
        RegistracijaRequestDTO dto = new RegistracijaRequestDTO();
        dto.setIme("Ana"); dto.setPrezime("Simic");
        dto.setEmail("ana3@test.ba"); dto.setLozinka("123");

        mockMvc.perform(post("/api/auth/registracija")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greska").value("VALIDATION_ERROR"));
    }

    @Test
    void registracija_duplikatEmail_vraca409() throws Exception {
        RegistracijaRequestDTO dto = new RegistracijaRequestDTO();
        dto.setIme("Ana"); dto.setPrezime("Simic");
        dto.setEmail("duplikat@test.ba"); dto.setLozinka("Lozinka123!");
        dto.setUloga(UlogaKorisnika.GRADJANIN);

        mockMvc.perform(post("/api/auth/registracija")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/registracija")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.greska").value("CONFLICT"));
    }

    @Test
    void dohvatiPoId_nePostoji_vraca404() throws Exception {
        mockMvc.perform(get("/api/korisnici/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }
}
