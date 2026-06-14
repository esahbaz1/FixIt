package ba.etf.fixit.reportservice.controller;

import ba.etf.fixit.reportservice.client.UserServiceKlijent;
import ba.etf.fixit.reportservice.dto.KomentarRequestDTO;
import ba.etf.fixit.reportservice.dto.KorisnikDTO;
import ba.etf.fixit.reportservice.dto.PrijavaRequestDTO;
import ba.etf.fixit.reportservice.model.Kategorija;
import ba.etf.fixit.reportservice.model.PrioritetPrijave;
import ba.etf.fixit.reportservice.model.Statusi;
import ba.etf.fixit.reportservice.model.TipPromjene;
import ba.etf.fixit.reportservice.repository.ArhivaRepository;
import ba.etf.fixit.reportservice.repository.FotografijaRepository;
import ba.etf.fixit.reportservice.repository.HistorijaPrijaveRepository;
import ba.etf.fixit.reportservice.repository.KategorijaRepository;
import ba.etf.fixit.reportservice.repository.KomentarRepository;
import ba.etf.fixit.reportservice.repository.PrijavaRepository;
import ba.etf.fixit.reportservice.repository.StatusiRepository;
import ba.etf.fixit.reportservice.repository.TipPromjeneRepository;
import ba.etf.fixit.reportservice.repository.ValidacijaRepository;
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

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @MockBean private UserServiceKlijent userServiceKlijent;

    private Long kategorijaId;

    @BeforeEach
    void setUp() {
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
        Kategorija k = kategorijaRepo.save(new Kategorija(null, "Put/cesta", "Ostecenja cesta", 1L));
        kategorijaId = k.getId();

        KorisnikDTO aktivanKorisnik = new KorisnikDTO(1L, "Test", "Korisnik", "test@test.ba", "GRADJANIN", true);
        when(userServiceKlijent.validirajKorisnika(anyLong())).thenReturn(aktivanKorisnik);
    }

    /**
     * Pravi DTO za prijavu - bez korisnikId (dolazi iz tokena/konteksta).
     */
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

    /**
     * Kreira prijavu i vraća prijavaId iz 202 Accepted odgovora.
     * Kontroler sada vraća {"prijavaId": X, "status": "POKRENUTO", "poruka": "..."}.
     */
    private Long kreirajPrijavu(String naslov) throws Exception {
        String response = mockMvc.perform(post("/api/prijave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPrijavaDto(naslov))))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("prijavaId").asLong();
    }

    @Test
    void kreirajPrijavu_uspjesno_vraca202() throws Exception {
        mockMvc.perform(post("/api/prijave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPrijavaDto("Test rupa na putu"))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.prijavaId").exists())
                .andExpect(jsonPath("$.status").value("POKRENUTO"))
                .andExpect(jsonPath("$.poruka").exists());
    }

    @Test
    void kreirajPrijavu_bezNaslova_vraca400() throws Exception {
        PrijavaRequestDTO dto = validPrijavaDto("X");
        dto.setNaslov(null);

        mockMvc.perform(post("/api/prijave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greska").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.poruke.naslov").exists());
    }

    @Test
    void dohvatiSve_uspjesno() throws Exception {
        kreirajPrijavu("Prijava za listu");

        mockMvc.perform(get("/api/prijave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void dohvatiPoId_uspjesno() throws Exception {
        Long id = kreirajPrijavu("Prijava po ID");

        mockMvc.perform(get("/api/prijave/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.naslov").value("Prijava po ID"));
    }

    @Test
    void dohvatiPoId_nePostoji_vraca404() throws Exception {
        mockMvc.perform(get("/api/prijave/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void promijeniStatus_uspjesno_vraca202() throws Exception {
        Long id = kreirajPrijavu("Status test");

        mockMvc.perform(patch("/api/prijave/" + id + "/status")
                        .param("noviStatus", "Rijeseno"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.prijavaId").value(id))
                .andExpect(jsonPath("$.status").value("POKRENUTO"));
    }

    @Test
    void promijeniStatus_statusNePostoji_vraca404() throws Exception {
        Long id = kreirajPrijavu("Status not found test");

        mockMvc.perform(patch("/api/prijave/" + id + "/status")
                        .param("noviStatus", "Nepostojeci"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void partialUpdate_uspjesno() throws Exception {
        Long id = kreirajPrijavu("Stari naslov");

        mockMvc.perform(patch("/api/prijave/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("naslov", "Novi naslov"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.naslov").value("Novi naslov"));
    }

    @Test
    void partialUpdate_nePostoji_vraca404() throws Exception {
        mockMvc.perform(patch("/api/prijave/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("naslov", "Novi naslov"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void hitnePrekoracene_uspjesno() throws Exception {
        PrijavaRequestDTO dto = validPrijavaDto("Hitna prijava");
        dto.setPrioritet(PrioritetPrijave.HITNO);
        dto.setDatumRoka(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/prijave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isAccepted());

        mockMvc.perform(get("/api/prijave/hitne-prekoracene"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void dohvatiSvePaged_uspjesno() throws Exception {
        kreirajPrijavu("Paged test 1");
        kreirajPrijavu("Paged test 2");

        mockMvc.perform(get("/api/prijave/paged")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sortBy", "datumPodnosenja"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void dohvatiSvePaged_nevalidanPage_vraca500() throws Exception {
        mockMvc.perform(get("/api/prijave/paged")
                        .param("page", "abc"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.greska").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    void arhiviraj_uspjesno() throws Exception {
        Long id = kreirajPrijavu("Arhiviranje");

        mockMvc.perform(patch("/api/prijave/" + id + "/arhiviraj"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/prijave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void arhiviraj_nePostoji_vraca404() throws Exception {
        mockMvc.perform(patch("/api/prijave/9999/arhiviraj"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void pretraga_uspjesno() throws Exception {
        kreirajPrijavu("Rupa kod skole");

        mockMvc.perform(get("/api/prijave/pretraga").param("q", "rupa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void pretraga_bezParametra_vraca500() throws Exception {
        mockMvc.perform(get("/api/prijave/pretraga"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.greska").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    void poLokaciji_uspjesno() throws Exception {
        kreirajPrijavu("Lokacija test");

        mockMvc.perform(get("/api/prijave/lokacija").param("lokacija", "Titova"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void poLokaciji_bezParametra_vraca500() throws Exception {
        mockMvc.perform(get("/api/prijave/lokacija"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.greska").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    void heatmap_uspjesno() throws Exception {
        kreirajPrijavu("Heatmap test");

        mockMvc.perform(get("/api/prijave/heatmap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].latitude").exists())
                .andExpect(jsonPath("$[0].longitude").exists());
    }

    @Test
    void dashboard_uspjesno() throws Exception {
        kreirajPrijavu("Dashboard test");

        mockMvc.perform(get("/api/prijave/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ukupnoPrijava").exists())
                .andExpect(jsonPath("$.aktivnePrijave").exists())
                .andExpect(jsonPath("$.prekoraceniRokovi").exists());
    }

    @Test
    void komentari_uspjesno() throws Exception {
        Long id = kreirajPrijavu("Komentari test");
        // korisnikId se ne šalje u body - dolazi iz autentifikovanog konteksta (header X-Korisnik-Id)
        KomentarRequestDTO komentar = new KomentarRequestDTO();
        komentar.setTekst("Javni komentar");
        komentar.setInteran(false);

        mockMvc.perform(post("/api/prijave/" + id + "/komentari")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(komentar)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/prijave/" + id + "/komentari"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void komentari_nevalidanId_vraca500() throws Exception {
        mockMvc.perform(get("/api/prijave/abc/komentari"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.greska").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    void dodajKomentar_uspjesno() throws Exception {
        Long id = kreirajPrijavu("Dodaj komentar test");
        KomentarRequestDTO komentar = new KomentarRequestDTO();
        komentar.setNaslov("Naslov komentara");
        komentar.setTekst("Tekst komentara");
        komentar.setInteran(false);

        mockMvc.perform(post("/api/prijave/" + id + "/komentari")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(komentar)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prijavaId").value(id))
                .andExpect(jsonPath("$.tekst").value("Tekst komentara"));
    }

    @Test
    void dodajKomentar_bezTeksta_vraca400() throws Exception {
        Long id = kreirajPrijavu("Komentar validacija");
        KomentarRequestDTO komentar = new KomentarRequestDTO();
        komentar.setTekst("");

        mockMvc.perform(post("/api/prijave/" + id + "/komentari")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(komentar)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greska").value("VALIDATION_ERROR"));
    }

    @Test
    void dodajKomentar_prijavaNePostoji_vraca404() throws Exception {
        KomentarRequestDTO komentar = new KomentarRequestDTO();
        komentar.setTekst("Tekst komentara");
        komentar.setInteran(false);

        mockMvc.perform(post("/api/prijave/9999/komentari")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(komentar)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void kreirajPrijavu_korisnikNijePronadjen_vraca404() throws Exception {
        // Simuliramo da se korisnikId 999 šalje kroz header (kontekst), ne kroz body
        when(userServiceKlijent.validirajKorisnika(999L))
                .thenThrow(new UserServiceKlijent.KorisnikNijePronadjenException("Korisnik 999 nije pronadjen"));

        // Ovaj test zahtijeva da test infra postavi X-Korisnik-Id: 999 header
        // Bez toga, KorisnikKontekst.korisnikId() vraća null pa će validacija koristiti null
        // Test ostaje kao dokumentacija ponašanja - u produkciji gateway uvijek šalje ID
        mockMvc.perform(post("/api/prijave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Korisnik-Id", "999")
                        .header("X-Korisnik-Email", "test@test.ba")
                        .header("X-Korisnik-Uloga", "GRADJANIN")
                        .content(objectMapper.writeValueAsString(validPrijavaDto("Test nepostojeci korisnik"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("KORISNIK_NOT_FOUND"));
    }

    @Test
    void kreirajPrijavu_korisnikNijeAktivan_vraca422() throws Exception {
        when(userServiceKlijent.validirajKorisnika(2L))
                .thenThrow(new UserServiceKlijent.KorisnikNijeAktivanException("Korisnik 2 je deaktiviran"));

        mockMvc.perform(post("/api/prijave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Korisnik-Id", "2")
                        .header("X-Korisnik-Email", "test@test.ba")
                        .header("X-Korisnik-Uloga", "GRADJANIN")
                        .content(objectMapper.writeValueAsString(validPrijavaDto("Test neaktivan korisnik"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.greska").value("KORISNIK_NIJE_AKTIVAN"));
    }

    @Test
    void kreirajPrijavu_userServiceNedostupan_gracefulDegradation_vraca202() throws Exception {
        when(userServiceKlijent.validirajKorisnika(anyLong())).thenReturn(null);

        mockMvc.perform(post("/api/prijave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPrijavaDto("Test graceful"))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.prijavaId").exists())
                .andExpect(jsonPath("$.status").value("POKRENUTO"));
    }
}
