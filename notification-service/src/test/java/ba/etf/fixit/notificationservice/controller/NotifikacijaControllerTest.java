package ba.etf.fixit.notificationservice.controller;

import ba.etf.fixit.notificationservice.dto.NotifikacijaRequestDTO;
import ba.etf.fixit.notificationservice.model.TipNotifikacije;
import ba.etf.fixit.notificationservice.repository.NotifikacijaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotifikacijaControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private NotifikacijaRepository repo;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
    }

    private NotifikacijaRequestDTO validDto(Long korisnikId, String naslov, TipNotifikacije tip) {
        NotifikacijaRequestDTO dto = new NotifikacijaRequestDTO();
        dto.setKorisnikId(korisnikId);
        dto.setPrijavaId(1L);
        dto.setNaslov(naslov);
        dto.setTekst("Test tekst");
        dto.setTip(tip);
        return dto;
    }

    @Test
    void kreirajNotifikaciju_uspjesno() throws Exception {
        NotifikacijaRequestDTO dto = validDto(1L, "Test naslov", TipNotifikacije.STATUS_PROMJENA);

        mockMvc.perform(post("/api/notifikacije")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.naslov").value("Test naslov"))
                .andExpect(jsonPath("$.procitano").value(false));
    }

    @Test
    void kreirajNotifikaciju_bezNaslova_vraca400() throws Exception {
        NotifikacijaRequestDTO dto = validDto(1L, "", TipNotifikacije.STATUS_PROMJENA);

        mockMvc.perform(post("/api/notifikacije")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greska").value("VALIDATION_ERROR"));
    }

    @Test
    void dohvatiZaKorisnika_uspjesno() throws Exception {
        mockMvc.perform(post("/api/notifikacije")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validDto(10L, "Naslov 1", TipNotifikacije.STATUS_PROMJENA))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/notifikacije/korisnik/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].korisnikId").value(10L));
    }

    @Test
    void dohvatiNeprocitane_uspjesno() throws Exception {
        mockMvc.perform(post("/api/notifikacije")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validDto(20L, "Naslov 2", TipNotifikacije.NOVI_KOMENTAR))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/notifikacije/korisnik/20/neprocitane"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void brojNeprocitanih_uspjesno() throws Exception {
        mockMvc.perform(post("/api/notifikacije")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validDto(30L, "Naslov 3", TipNotifikacije.NOVI_KOMENTAR))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/notifikacije/korisnik/30/broj-neprocitanih"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brojNeprocitanih").value(1));
    }

    @Test
    void oznaci_uspjesno() throws Exception {
        String response = mockMvc.perform(post("/api/notifikacije")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validDto(40L, "Naslov 4", TipNotifikacije.RIJESENO))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/notifikacije/" + id + "/procitano"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.procitano").value(true));
    }

    @Test
    void oznaci_nePostoji_vraca404() throws Exception {
        mockMvc.perform(patch("/api/notifikacije/9999/procitano"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void paged_uspjesno() throws Exception {
        mockMvc.perform(post("/api/notifikacije")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validDto(50L, "Naslov A", TipNotifikacije.NOVA_PRIJAVA))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/notifikacije")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validDto(50L, "Naslov B", TipNotifikacije.NOVA_PRIJAVA))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/notifikacije/korisnik/50/paged")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void paged_nevalidanPage_vraca500() throws Exception {
        mockMvc.perform(get("/api/notifikacije/korisnik/50/paged")
                        .param("page", "x"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.greska").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    void neprocitanePoTipu_uspjesno() throws Exception {
        mockMvc.perform(post("/api/notifikacije")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validDto(60L, "Komentar", TipNotifikacije.NOVI_KOMENTAR))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/notifikacije")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validDto(60L, "Status", TipNotifikacije.STATUS_PROMJENA))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/notifikacije/korisnik/60/neprocitane-tip")
                        .param("tip", "NOVI_KOMENTAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tip").value("NOVI_KOMENTAR"));
    }

    @Test
    void neprocitanePoTipu_nevalidanTip_vraca500() throws Exception {
        mockMvc.perform(get("/api/notifikacije/korisnik/60/neprocitane-tip")
                        .param("tip", "POGRESAN_TIP"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.greska").value("INTERNAL_SERVER_ERROR"));
    }
}
