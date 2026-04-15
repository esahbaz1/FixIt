package ba.etf.fixit.reportservice.controller;

import ba.etf.fixit.reportservice.dto.PrijavaRequestDTO;
import ba.etf.fixit.reportservice.model.*;
import ba.etf.fixit.reportservice.repository.*;
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
class PrijavaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PrijavaRepository prijavaRepo;
    @Autowired private KategorijaRepository kategorijaRepo;
    @Autowired private StatusiRepository statusiRepo;
    @Autowired private TipPromjeneRepository tipRepo;
    @Autowired private ArhivaRepository arhivaRepo;
    @Autowired private KomentarRepository komentarRepo;
    @Autowired private FotografijaRepository fotografijaRepo;
    @Autowired private HistorijaPrijaveRepository historijaRepo;
    @Autowired private ValidacijaRepository validacijaRepo;

    private Long kategorijaId;

    @BeforeEach
    void setUp() {
        // Brisati u ispravnom redoslijedu - prvo zavisne tabele
        arhivaRepo.deleteAll();
        validacijaRepo.deleteAll();
        historijaRepo.deleteAll();
        komentarRepo.deleteAll();
        fotografijaRepo.deleteAll();
        prijavaRepo.deleteAll();
        kategorijaRepo.deleteAll();
        statusiRepo.deleteAll();
        tipRepo.deleteAll();

        // Kreiranje potrebnih podataka za testove
        tipRepo.save(new TipPromjene(null, "Novo"));
        statusiRepo.save(new Statusi("Novo", "Novoprijavljen problem"));
        Kategorija k = kategorijaRepo.save(new Kategorija("Put/cesta", "Ostecenja cesta", 1L));
        kategorijaId = k.getId();
    }

    @Test
    void kreirajPrijavu_uspjesno() throws Exception {
        PrijavaRequestDTO dto = new PrijavaRequestDTO();
        dto.setNaslov("Test rupa na putu");
        dto.setOpis("Velika rupa na raskrsnici");
        dto.setLatitude(43.85);
        dto.setLongitude(18.41);
        dto.setAdresa("Titova ulica, Sarajevo");
        dto.setKategorijaId(kategorijaId);
        dto.setKorisnikId(1L);

        mockMvc.perform(post("/api/prijave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.naslov").value("Test rupa na putu"))
                .andExpect(jsonPath("$.statusNaziv").value("Novo"));
    }

    @Test
    void kreirajPrijavu_bezNaslova_vraca400() throws Exception {
        PrijavaRequestDTO dto = new PrijavaRequestDTO();
        dto.setOpis("Opis bez naslova");
        dto.setLatitude(43.85);
        dto.setLongitude(18.41);
        dto.setKategorijaId(kategorijaId);
        dto.setKorisnikId(1L);

        mockMvc.perform(post("/api/prijave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greska").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.poruke.naslov").exists());
    }

    @Test
    void dohvatiPoId_nePostoji_vraca404() throws Exception {
        mockMvc.perform(get("/api/prijave/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }
}