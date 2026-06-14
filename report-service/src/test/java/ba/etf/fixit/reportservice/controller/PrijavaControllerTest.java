package ba.etf.fixit.reportservice.controller;

import ba.etf.fixit.reportservice.client.UserServiceKlijent;
import ba.etf.fixit.reportservice.dto.*;
import ba.etf.fixit.reportservice.model.*;
import ba.etf.fixit.reportservice.repository.*;
import ba.etf.fixit.reportservice.security.KorisnikKontekst;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) 
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

    @MockBean private UserServiceKlijent userServiceKlijent;

    private Long kategorijaId;

  
    private void setKontekst() {
        KorisnikKontekst.postavi(
                new KorisnikKontekst.KorisnikPodaci(
                        1L,
                        "test@test.ba",
                        "GRADJANIN"
                )
        );
    }

    private void setKontekstRadnik() {
        KorisnikKontekst.postavi(
                new KorisnikKontekst.KorisnikPodaci(
                        1L,
                        "radnik@test.ba",
                        "RADNIK"
                )
        );
    }

    @BeforeEach
    void setUp() {
       
        KorisnikKontekst.obrisi();

        arhivaRepo.deleteAll();
        validacijaRepo.deleteAll();
        historijaRepo.deleteAll();
        komentarRepo.deleteAll();
        fotografijaRepo.deleteAll();
        prijavaRepo.deleteAll();
        kategorijaRepo.deleteAll();
        statusiRepo.deleteAll();
        tipRepo.deleteAll();

        tipRepo.save(new TipPromjene(null, null, "Novo"));
        tipRepo.save(new TipPromjene(null, "Novo", "Rijeseno"));

        statusiRepo.save(new Statusi(null, "Novo", "Novo prijavljen problem"));
        statusiRepo.save(new Statusi(null, "Rijeseno", "Zavrseno"));

        Kategorija k = kategorijaRepo.save(
                new Kategorija(null, "Put/cesta", "Ostecenja cesta", 1L)
        );
        kategorijaId = k.getId();

        KorisnikDTO aktivanKorisnik =
                new KorisnikDTO(1L, "Test", "Korisnik", "test@test.ba", "GRADJANIN", true);

        when(userServiceKlijent.validirajKorisnika(anyLong()))
                .thenReturn(aktivanKorisnik);
    }

    private PrijavaRequestDTO validPrijavaDto(String naslov) {
        PrijavaRequestDTO dto = new PrijavaRequestDTO();
        dto.setNaslov(naslov);
        dto.setOpis("Velika rupa na raskrsnici");
        dto.setLatitude(43.85);
        dto.setLongitude(18.41);
        dto.setAdresa("Titova ulica, Sarajevo");
        dto.setKategorijaId(kategorijaId);
        dto.setPrioritet(PrioritetPrijave.SREDNJI);
        return dto;
    }

    
    private Long kreirajPrijavu(String naslov) throws Exception {

        setKontekst();

        String response = mockMvc.perform(post("/api/prijave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPrijavaDto(naslov))))
                .andExpect(status().isAccepted())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("prijavaId").asLong();
    }

    @Test
    void kreirajPrijavu_uspjesno_vraca202() throws Exception {
        setKontekst();

        mockMvc.perform(post("/api/prijave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPrijavaDto("Test"))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.prijavaId").exists())
                .andExpect(jsonPath("$.status").value("POKRENUTO"));
    }

    @Test
    void dohvatiSve_uspjesno() throws Exception {
        kreirajPrijavu("Test");

        mockMvc.perform(get("/api/prijave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void dohvatiPoId_uspjesno() throws Exception {
        Long id = kreirajPrijavu("Test");

        mockMvc.perform(get("/api/prijave/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void dohvatiPoId_nePostoji_vraca404() throws Exception {
        setKontekst();

        mockMvc.perform(get("/api/prijave/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void partialUpdate_uspjesno() throws Exception {
        Long id = kreirajPrijavu("Stari");

        setKontekstRadnik();

        mockMvc.perform(patch("/api/prijave/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("naslov", "Novi"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naslov").value("Novi"));
    }

    @Test
    void arhiviraj_uspjesno() throws Exception {
        Long id = kreirajPrijavu("Arhiva");

        setKontekstRadnik();

        mockMvc.perform(patch("/api/prijave/" + id + "/arhiviraj"))
                .andExpect(status().isNoContent());
    }

    @Test
    void pretraga_uspjesno() throws Exception {
        kreirajPrijavu("Rupa kod skole");

        mockMvc.perform(get("/api/prijave/pretraga").param("q", "rupa"))
                .andExpect(status().isOk());
    }

    @Test
    void heatmap_uspjesno() throws Exception {
        kreirajPrijavu("Heatmap");

        mockMvc.perform(get("/api/prijave/heatmap"))
                .andExpect(status().isOk());
    }

    @Test
    void dashboard_uspjesno() throws Exception {
        kreirajPrijavu("Dashboard");

        mockMvc.perform(get("/api/prijave/dashboard"))
                .andExpect(status().isOk());
    }




@Test
void dohvatiSvePaged_uspjesno() throws Exception {
    kreirajPrijavu("P1");
    kreirajPrijavu("P2");

    mockMvc.perform(get("/api/prijave/paged")
                    .param("page", "0")
                    .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
}

@Test
void historija_uspjesno() throws Exception {
    Long id = kreirajPrijavu("Historija");

    mockMvc.perform(get("/api/prijave/" + id + "/historija"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
}


}