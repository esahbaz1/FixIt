package ba.etf.fixit.managementservice.controller;

import ba.etf.fixit.managementservice.dto.RadnikRequestDTO;
import ba.etf.fixit.managementservice.model.GradskaSluzba;
import ba.etf.fixit.managementservice.model.Radnik;
import ba.etf.fixit.managementservice.repository.GradskaSluzbaRepository;
import ba.etf.fixit.managementservice.repository.KorisnikProfilRepository;
import ba.etf.fixit.managementservice.repository.RadnikRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RadnikControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RadnikRepository radnikRepo;
    @Autowired private GradskaSluzbaRepository sluzbaRepo;
    @Autowired private KorisnikProfilRepository profilRepo;

    private Long sluzbaId;
    private Long drugaSluzbaId;

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
        KorisnikKontekst.obrisi();
        radnikRepo.deleteAll();
        profilRepo.deleteAll();
        sluzbaRepo.deleteAll();

        GradskaSluzba sluzba = sluzbaRepo.save(new GradskaSluzba(
                null, "JKP Test", "Testna sluzba", "jkp@test.ba", "033-000-000", true));
        GradskaSluzba druga = sluzbaRepo.save(new GradskaSluzba(
                null, "JKP Druga", "Druga sluzba", "druga@test.ba", "033-111-000", true));
        sluzbaId = sluzba.getId();
        drugaSluzbaId = druga.getId();
    }

    @Test
    void kreirajRadnika_uspjesno() throws Exception {
        setKontekstAdmin();
        RadnikRequestDTO dto = new RadnikRequestDTO();
        dto.setKorisnikId(1L);
        dto.setGradskaSluzbaId(sluzbaId);
        dto.setPozicija("Inspektor");
        dto.setKompetencije("Putevi");

        mockMvc.perform(post("/api/radnici")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.korisnikId").value(1L))
                .andExpect(jsonPath("$.nazivSluzbe").value("JKP Test"));
    }

    @Test
    void dohvatiSveRadnike_uspjesno() throws Exception {
        setKontekstAdmin();
        mockMvc.perform(get("/api/radnici"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void dohvatiPoId_uspjesno() throws Exception {
        setKontekstAdmin();
        Radnik radnik = sluzbaRepo.findById(sluzbaId)
                .map(s -> radnikRepo.save(new Radnik(null, 7L, s, "Inspektor", "A", true)))
                .orElseThrow();

        mockMvc.perform(get("/api/radnici/" + radnik.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(radnik.getId()))
                .andExpect(jsonPath("$.korisnikId").value(7L));
    }

    @Test
    void dohvatiPoId_nePostoji_vraca404() throws Exception {
        setKontekstAdmin();
        mockMvc.perform(get("/api/radnici/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void dohvatiPoSluzbi_uspjesno() throws Exception {
        setKontekstAdmin();
        sluzbaRepo.findById(sluzbaId)
                .ifPresent(s -> radnikRepo.save(new Radnik(null, 5L, s, "Vozac", null, true)));

        mockMvc.perform(get("/api/radnici/sluzba/" + sluzbaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void paged_uspjesno() throws Exception {
        setKontekstAdmin();
        sluzbaRepo.findById(sluzbaId).ifPresent(s -> {
            radnikRepo.save(new Radnik(null, 11L, s, "Inspektor", "A", true));
            radnikRepo.save(new Radnik(null, 12L, s, "Inspektor", "B", true));
        });

        mockMvc.perform(get("/api/radnici/sluzba/" + sluzbaId + "/paged")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void paged_nevalidanPage_vraca500() throws Exception {
        setKontekstAdmin();
        mockMvc.perform(get("/api/radnici/sluzba/" + sluzbaId + "/paged")
                        .param("page", "abc"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.greska").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    void aktivniPoPoziciji_uspjesno() throws Exception {
        setKontekstAdmin();
        sluzbaRepo.findById(sluzbaId).ifPresent(s -> {
            radnikRepo.save(new Radnik(null, 13L, s, "Inspektor", "A", true));
            radnikRepo.save(new Radnik(null, 14L, s, "Vozac", "B", true));
        });

        mockMvc.perform(get("/api/radnici/sluzba/" + sluzbaId + "/aktivni")
                        .param("pozicija", "Inspektor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].pozicija").value("Inspektor"));
    }

    @Test
    void aktivniPoPoziciji_bezParametra_vraca500() throws Exception {
        setKontekstAdmin();
        mockMvc.perform(get("/api/radnici/sluzba/" + sluzbaId + "/aktivni"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.greska").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    void obrisiRadnika_uspjesno() throws Exception {
        setKontekstAdmin();
        Radnik radnik = sluzbaRepo.findById(sluzbaId)
                .map(s -> radnikRepo.save(new Radnik(null, 8L, s, "Terenski", "A", true)))
                .orElseThrow();

        mockMvc.perform(delete("/api/radnici/" + radnik.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/radnici/" + radnik.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void obrisiRadnika_nePostoji_vraca404() throws Exception {
        setKontekstAdmin();
        mockMvc.perform(delete("/api/radnici/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void premjesti_uspjesno() throws Exception {
        setKontekstAdmin();
        Radnik radnik = sluzbaRepo.findById(sluzbaId)
                .map(s -> radnikRepo.save(new Radnik(null, 15L, s, "Inspektor", "A", true)))
                .orElseThrow();

        mockMvc.perform(put("/api/radnici/" + radnik.getId() + "/premjesti/" + drugaSluzbaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradskaSluzbaId").value(drugaSluzbaId))
                .andExpect(jsonPath("$.nazivSluzbe").value("JKP Druga"));
    }

    @Test
    void premjesti_radnikNePostoji_vraca404() throws Exception {
        setKontekstAdmin();
        mockMvc.perform(put("/api/radnici/9999/premjesti/" + drugaSluzbaId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void premjesti_sluzbaNePostoji_vraca404() throws Exception {
        setKontekstAdmin();
        Radnik radnik = sluzbaRepo.findById(sluzbaId)
                .map(s -> radnikRepo.save(new Radnik(null, 16L, s, "Inspektor", "A", true)))
                .orElseThrow();

        mockMvc.perform(put("/api/radnici/" + radnik.getId() + "/premjesti/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void kreirajRadnika_bezKorisnikId_vraca400() throws Exception {
        setKontekstAdmin();
        RadnikRequestDTO dto = new RadnikRequestDTO();
        dto.setGradskaSluzbaId(sluzbaId);

        mockMvc.perform(post("/api/radnici")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greska").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.poruke.korisnikId").exists());
    }

    @Test
    void kreirajRadnika_sluzbaNePostoji_vraca404() throws Exception {
        setKontekstAdmin();
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
void dohvatiPoKorisniku_uspjesno() throws Exception {
    setKontekstAdmin();

    Radnik radnik = sluzbaRepo.findById(sluzbaId)
            .map(s -> radnikRepo.save(
                    new Radnik(null, 77L, s, "Inspektor", "A", true)))
            .orElseThrow();

    mockMvc.perform(get("/api/radnici/korisnik/77"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.korisnikId").value(77L));
}


@Test
void dohvatiPoKorisniku_nePostoji_vraca404() throws Exception {
    setKontekstAdmin();

    mockMvc.perform(get("/api/radnici/korisnik/9999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
}

@Test
void dodijeliNaPrijavu_radnikNePostoji_vraca404() throws Exception {
    setKontekstAdmin();

    mockMvc.perform(post("/api/radnici/9999/prijave/1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
}
}
