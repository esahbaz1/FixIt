package ba.etf.fixit.managementservice.controller;

import ba.etf.fixit.managementservice.dto.GradskaSluzbaRequestDTO;
import ba.etf.fixit.managementservice.repository.GradskaSluzbaRepository;
import ba.etf.fixit.managementservice.repository.RadnikRepository;
import ba.etf.fixit.managementservice.repository.KorisnikProfilRepository;
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
class GradskaSluzbaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private GradskaSluzbaRepository repo;
    @Autowired private RadnikRepository radnikRepo;
    @Autowired private KorisnikProfilRepository profilRepo;

    @BeforeEach
    void setUp() {
        // Brisati u ispravnom redoslijedu - prvo zavisne tabele
        radnikRepo.deleteAll();
        profilRepo.deleteAll();
        repo.deleteAll();
    }

    @Test
    void kreirajSluzbu_uspjesno() throws Exception {
        GradskaSluzbaRequestDTO dto = new GradskaSluzbaRequestDTO();
        dto.setNaziv("Test JKP");
        dto.setOpis("Opis");
        dto.setKontaktEmail("test@jkp.ba");
        dto.setKontaktTelefon("033-000-000");

        mockMvc.perform(post("/api/gradske-sluzbe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.naziv").value("Test JKP"));
    }

    @Test
    void kreirajSluzbu_bezNaziva_vraca400() throws Exception {
        GradskaSluzbaRequestDTO dto = new GradskaSluzbaRequestDTO();
        dto.setOpis("Bez naziva");

        mockMvc.perform(post("/api/gradske-sluzbe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greska").value("VALIDATION_ERROR"));
    }

    @Test
    void dohvatiPoId_nePostoji_vraca404() throws Exception {
        mockMvc.perform(get("/api/gradske-sluzbe/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void dohvatiSve_uspjesno() throws Exception {
        mockMvc.perform(get("/api/gradske-sluzbe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}