package ba.etf.fixit.notificationservice.controller;
import ba.etf.fixit.notificationservice.dto.NotifikacijaRequestDTO;
import ba.etf.fixit.notificationservice.model.TipNotifikacije;
import ba.etf.fixit.notificationservice.repository.NotifikacijaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class NotifikacijaControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private NotifikacijaRepository repo;

    @BeforeEach void setUp(){ repo.deleteAll(); }

    @Test void kreirajNotifikaciju_uspjesno() throws Exception {
        NotifikacijaRequestDTO dto = new NotifikacijaRequestDTO();
        dto.setKorisnikId(1L); dto.setPrijavaId(1L);
        dto.setNaslov("Test naslov"); dto.setTekst("Test tekst");
        dto.setTip(TipNotifikacije.STATUS_PROMJENA);
        mockMvc.perform(post("/api/notifikacije").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.naslov").value("Test naslov"))
                .andExpect(jsonPath("$.procitano").value(false));
    }

    @Test void kreirajNotifikaciju_bezNaslova_vraca400() throws Exception {
        NotifikacijaRequestDTO dto = new NotifikacijaRequestDTO();
        dto.setKorisnikId(1L); dto.setTekst("Tekst"); dto.setTip(TipNotifikacije.STATUS_PROMJENA);
        mockMvc.perform(post("/api/notifikacije").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greska").value("VALIDATION_ERROR"));
    }

    @Test void oznaci_nePostoji_vraca404() throws Exception {
        mockMvc.perform(patch("/api/notifikacije/9999/procitano"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test void dohvatiZaKorisnika_uspjesno() throws Exception {
        mockMvc.perform(get("/api/notifikacije/korisnik/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
    }
}
