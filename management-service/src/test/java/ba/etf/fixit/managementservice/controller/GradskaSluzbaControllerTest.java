package ba.etf.fixit.managementservice.controller;

import ba.etf.fixit.managementservice.dto.GradskaSluzbaRequestDTO;
import ba.etf.fixit.managementservice.model.GradskaSluzba;
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
import ba.etf.fixit.managementservice.security.KorisnikKontekst;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class GradskaSluzbaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private GradskaSluzbaRepository repo;
    @Autowired private RadnikRepository radnikRepo;
    @Autowired private KorisnikProfilRepository profilRepo;
    private void setKontekstAdmin() {
    KorisnikKontekst.postavi(
            new KorisnikKontekst.KorisnikPodaci(
                    1L,
                    "admin@test.ba",
                    "ADMIN"
            )
    );
}

    @BeforeEach
    void setUp() {
        // Brisati u ispravnom redoslijedu - prvo zavisne tabele
        KorisnikKontekst.obrisi();
        radnikRepo.deleteAll();
        profilRepo.deleteAll();
        repo.deleteAll();
    }

    @Test
    void kreirajSluzbu_uspjesno() throws Exception {
        setKontekstAdmin();
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
        setKontekstAdmin();
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
        setKontekstAdmin();
        mockMvc.perform(get("/api/gradske-sluzbe/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void dohvatiPoId_uspjesno() throws Exception {
        setKontekstAdmin();
        GradskaSluzba sluzba = repo.save(new GradskaSluzba(
                null, "Vodovod", "Opis", "voda@test.ba", "033-123-123", true));

        mockMvc.perform(get("/api/gradske-sluzbe/" + sluzba.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sluzba.getId()))
                .andExpect(jsonPath("$.naziv").value("Vodovod"));
    }

    @Test
    void dohvatiSve_uspjesno() throws Exception {
        setKontekstAdmin();
        mockMvc.perform(get("/api/gradske-sluzbe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void azuriraj_uspjesno() throws Exception {
        setKontekstAdmin();
        GradskaSluzba sluzba = repo.save(new GradskaSluzba(
                null, "Stari naziv", "Opis", "stari@test.ba", "033-111-111", true));

        GradskaSluzbaRequestDTO dto = new GradskaSluzbaRequestDTO();
        dto.setNaziv("Novi naziv");
        dto.setOpis("Novi opis");
        dto.setKontaktEmail("novi@test.ba");
        dto.setKontaktTelefon("033-222-222");

        mockMvc.perform(put("/api/gradske-sluzbe/" + sluzba.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naziv").value("Novi naziv"))
                .andExpect(jsonPath("$.kontaktEmail").value("novi@test.ba"));
    }

    @Test
    void azuriraj_nePostoji_vraca404() throws Exception {
        setKontekstAdmin();
        GradskaSluzbaRequestDTO dto = new GradskaSluzbaRequestDTO();
        dto.setNaziv("Novi naziv");

        mockMvc.perform(put("/api/gradske-sluzbe/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void obrisi_uspjesno() throws Exception {
        setKontekstAdmin();
        GradskaSluzba sluzba = repo.save(new GradskaSluzba(
                null, "Za brisanje", "Opis", "obrisi@test.ba", "033-555-555", true));

        mockMvc.perform(delete("/api/gradske-sluzbe/" + sluzba.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/gradske-sluzbe/" + sluzba.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void obrisi_nePostoji_vraca404() throws Exception {
        setKontekstAdmin();
        mockMvc.perform(delete("/api/gradske-sluzbe/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }


    @Test
void kreirajSluzbu_duplikatNaziv_vraca409() throws Exception {
    setKontekstAdmin();

    repo.save(new GradskaSluzba(
            null,
            "Vodovod",
            "Opis",
            "voda@test.ba",
            "033-111-111",
            true));

    GradskaSluzbaRequestDTO dto = new GradskaSluzbaRequestDTO();
    dto.setNaziv("Vodovod");
    dto.setOpis("Novi opis");
    dto.setKontaktEmail("novi@test.ba");
    dto.setKontaktTelefon("033-222-222");

    mockMvc.perform(post("/api/gradske-sluzbe")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.greska").value("CONFLICT"));
}



@Test
void azuriraj_bezNaziva_vraca400() throws Exception {
    setKontekstAdmin();

    GradskaSluzba sluzba = repo.save(new GradskaSluzba(
            null,
            "Vodovod",
            "Opis",
            "voda@test.ba",
            "033-111-111",
            true));

    GradskaSluzbaRequestDTO dto = new GradskaSluzbaRequestDTO();
    dto.setNaziv(null);

    mockMvc.perform(put("/api/gradske-sluzbe/" + sluzba.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.greska").value("VALIDATION_ERROR"));
}
}
