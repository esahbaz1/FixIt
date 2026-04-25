package ba.etf.fixit.managementservice.controller;

import ba.etf.fixit.managementservice.dto.RadnikRequestDTO;
import ba.etf.fixit.managementservice.model.GradskaSluzba;
import ba.etf.fixit.managementservice.model.Radnik;
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
class RadnikControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RadnikRepository radnikRepo;
    @Autowired private GradskaSluzbaRepository sluzbaRepo;
    @Autowired private KorisnikProfilRepository profilRepo;

    private Long sluzbaId;

    @BeforeEach
    void setUp() {
        radnikRepo.deleteAll();
        profilRepo.deleteAll();
        sluzbaRepo.deleteAll();

        GradskaSluzba sluzba = sluzbaRepo.save(
                new GradskaSluzba(null, "JKP Test", "Testna služba", "jkp@test.ba", "033-000-000", true));
        sluzbaId = sluzba.getId();
    }

    // --- USPJEŠNI ZAHTJEVI ---

    @Test
    void kreirajRadnika_uspjesno() throws Exception {
        RadnikRequestDTO dto = new RadnikRequestDTO();
        dto.setKorisnikId(1L);
        dto.setGradskaSluzbaId(sluzbaId);
        dto.setPozicija("Inspektor");
        dto.setKompetencije("Putevi i saobraćaj");

        mockMvc.perform(post("/api/radnici")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.korisnikId").value(1L))
                .andExpect(jsonPath("$.pozicija").value("Inspektor"))
                .andExpect(jsonPath("$.nazivSluzbe").value("JKP Test"));
    }

    @Test
    void dohvatiSveRadnike_uspjesno() throws Exception {
        mockMvc.perform(get("/api/radnici"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void dohvatiPoSluzbi_uspjesno() throws Exception {
        sluzbaRepo.findById(sluzbaId).ifPresent(s ->
                radnikRepo.save(new Radnik(null, 5L, s, "Vozač", null, true)));

        mockMvc.perform(get("/api/radnici/sluzba/" + sluzbaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // --- NEUSPJEŠNI ZAHTJEVI ---

    @Test
    void kreirajRadnika_bezKorisnikId_vraca400() throws Exception {
        RadnikRequestDTO dto = new RadnikRequestDTO();
        dto.setGradskaSluzbaId(sluzbaId);
        // korisnikId je null — treba baciti validacijsku grešku

        mockMvc.perform(post("/api/radnici")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greska").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.poruke.korisnikId").exists());
    }

    @Test
    void kreirajRadnika_sluzbaNePostoji_vraca404() throws Exception {
        RadnikRequestDTO dto = new RadnikRequestDTO();
        dto.setKorisnikId(1L);
        dto.setGradskaSluzbaId(9999L);

        mockMvc.perform(post("/api/radnici")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void dohvatiPoId_nePostoji_vraca404() throws Exception {
        mockMvc.perform(get("/api/radnici/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void obrisiRadnika_nePostoji_vraca404() throws Exception {
        mockMvc.perform(delete("/api/radnici/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }
}
