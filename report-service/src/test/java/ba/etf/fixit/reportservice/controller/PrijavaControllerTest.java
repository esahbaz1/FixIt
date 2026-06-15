package ba.etf.fixit.reportservice.controller;

import ba.etf.fixit.reportservice.client.ManagementServiceKlijent;
import ba.etf.fixit.reportservice.client.UserServiceKlijent;
import ba.etf.fixit.reportservice.dto.*;
import ba.etf.fixit.reportservice.model.*;
import ba.etf.fixit.reportservice.repository.*;
import ba.etf.fixit.reportservice.security.KorisnikKontekst;
import ba.etf.fixit.reportservice.service.StatistikaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
    @MockBean private ManagementServiceKlijent managementServiceKlijent;
    @MockBean private RabbitTemplate rabbitTemplate;
    @MockBean private StatistikaService statistikaService;

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

        // StatistikaService je mockovan jer koristi MySQL-specifične funkcije
        // (TIMESTAMPDIFF, DATE_FORMAT) koje H2 ne podržava
        StatistikaResponseDTO stubStatistika = new StatistikaResponseDTO(
                0L, 0L, 0L, 0L,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), null);
        when(statistikaService.dohvatiStatistiku()).thenReturn(stubStatistika);
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

// --- PATCH /{id}/status --------------------------------------------------

@Test
void promijeniStatus_uspjesno_vraca202() throws Exception {
    Long id = kreirajPrijavu("Status test");
    setKontekstRadnik();

    mockMvc.perform(patch("/api/prijave/" + id + "/status")
                    .param("noviStatus", "Rijeseno"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.status").value("POKRENUTO"))
            .andExpect(jsonPath("$.prijavaId").value(id));
}

@Test
void promijeniStatus_nePostojiPrijava_vraca404() throws Exception {
    setKontekstRadnik();

    mockMvc.perform(patch("/api/prijave/9999/status")
                    .param("noviStatus", "Rijeseno"))
            .andExpect(status().isNotFound());
}

@Test
void promijeniStatus_nePostojiStatus_vraca404() throws Exception {
    Long id = kreirajPrijavu("Status nevalidan");
    setKontekstRadnik();

    mockMvc.perform(patch("/api/prijave/" + id + "/status")
                    .param("noviStatus", "NepostojeciStatus"))
            .andExpect(status().isNotFound());
}

// --- PATCH /{id}/dodjeli-sluzbu ------------------------------------------

@Test
void dodijeliSluzbu_uspjesno() throws Exception {
    Long id = kreirajPrijavu("Dodjela sluzbi");
    setKontekstRadnik();

    mockMvc.perform(patch("/api/prijave/" + id + "/dodjeli-sluzbu")
                    .param("sluzbaId", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id));
}

@Test
void dodijeliSluzbu_nePostojiPrijava_vraca404() throws Exception {
    setKontekstRadnik();

    mockMvc.perform(patch("/api/prijave/9999/dodjeli-sluzbu")
                    .param("sluzbaId", "10"))
            .andExpect(status().isNotFound());
}

// --- PATCH /{id}/dodjeli-radnika -----------------------------------------

@Test
void dodijeliRadnika_uspjesno() throws Exception {
    Long id = kreirajPrijavu("Dodjela radnika");
    setKontekstRadnik();

    mockMvc.perform(patch("/api/prijave/" + id + "/dodjeli-radnika")
                    .param("korisnikId", "42"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id));
}

@Test
void dodijeliRadnika_nePostojiPrijava_vraca404() throws Exception {
    setKontekstRadnik();

    mockMvc.perform(patch("/api/prijave/9999/dodjeli-radnika")
                    .param("korisnikId", "42"))
            .andExpect(status().isNotFound());
}

// --- GET /lokacija -------------------------------------------------------

@Test
void poLokaciji_pronadjePrijave() throws Exception {
    kreirajPrijavu("Rupa u cesti");  // adresa = "Titova ulica, Sarajevo"

    mockMvc.perform(get("/api/prijave/lokacija")
                    .param("lokacija", "Titova"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].adresa").value("Titova ulica, Sarajevo"));
}

@Test
void poLokaciji_nePostojiLokacija_vratiPrazanNiz() throws Exception {
    kreirajPrijavu("Lokacija test");

    mockMvc.perform(get("/api/prijave/lokacija")
                    .param("lokacija", "NepostojecaUlica123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
}

// --- GET /hitne-prekoracene ----------------------------------------------

@Test
void hitnePrekoracene_bezHitnihVratiPrazanNiz() throws Exception {
    // SREDNJI prioritet, bez roka - ne smije biti u rezultatu
    kreirajPrijavu("Obicna prijava");

    mockMvc.perform(get("/api/prijave/hitne-prekoracene"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
}

@Test
void hitnePrekoracene_saHitnomPrekoracenomVratiJe() throws Exception {
    setKontekst();

    // Kreiramo HITNO prijavu sa rokom koji je već prošao
    PrijavaRequestDTO dto = validPrijavaDto("Hitna stvar");
    dto.setPrioritet(PrioritetPrijave.HITNO);
    dto.setDatumRoka(java.time.LocalDateTime.now().minusDays(1));

    mockMvc.perform(post("/api/prijave")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isAccepted());

    mockMvc.perform(get("/api/prijave/hitne-prekoracene"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].prioritet").value("HITNO"));
}

// --- GET /statistika -----------------------------------------------------

@Test
void statistika_uspjesno_vratiDTO() throws Exception {
    kreirajPrijavu("Za statistiku");

    mockMvc.perform(get("/api/prijave/statistika"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ukupnoPrijava").exists())
            .andExpect(jsonPath("$.aktivnePrijave").exists())
            .andExpect(jsonPath("$.arhiviranePrijave").exists())
            .andExpect(jsonPath("$.prekoraceniRokovi").exists())
            .andExpect(jsonPath("$.poKategorijama").exists())
            .andExpect(jsonPath("$.poStatusima").exists());
}

// --- GET /{id}/komentari i /interni --------------------------------------

@Test
void komentari_javni_uspjesno() throws Exception {
    Long id = kreirajPrijavu("Komentari test");
    setKontekst();

    KomentarRequestDTO dto = new KomentarRequestDTO("Naslov", "Javni komentar", false);
    mockMvc.perform(post("/api/prijave/" + id + "/komentari")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated());

    mockMvc.perform(get("/api/prijave/" + id + "/komentari"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].tekst").value("Javni komentar"))
            .andExpect(jsonPath("$[0].interan").value(false));
}

@Test
void komentari_interni_uspjesno() throws Exception {
    Long id = kreirajPrijavu("Interni komentar test");
    setKontekstRadnik();

    KomentarRequestDTO dto = new KomentarRequestDTO("Interni", "Interni tekst", true);
    mockMvc.perform(post("/api/prijave/" + id + "/komentari")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated());

    mockMvc.perform(get("/api/prijave/" + id + "/komentari/interni"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].interan").value(true));
}

@Test
void komentari_javljaJavniNieInteran() throws Exception {
    Long id = kreirajPrijavu("Odvajanje komentara");
    setKontekst();

    // Dodaj jedan javni i jedan interni
    KomentarRequestDTO javni = new KomentarRequestDTO("J", "Javni", false);
    KomentarRequestDTO interni = new KomentarRequestDTO("I", "Interni", true);

    mockMvc.perform(post("/api/prijave/" + id + "/komentari")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(javni)))
            .andExpect(status().isCreated());
    mockMvc.perform(post("/api/prijave/" + id + "/komentari")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(interni)))
            .andExpect(status().isCreated());

    // GET /komentari - samo javni
    mockMvc.perform(get("/api/prijave/" + id + "/komentari"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].interan").value(false));

    // GET /komentari/interni - samo interni
    mockMvc.perform(get("/api/prijave/" + id + "/komentari/interni"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].interan").value(true));
}

// --- POST /{id}/komentari ------------------------------------------------

@Test
void dodajKomentar_uspjesno_vraca201() throws Exception {
    Long id = kreirajPrijavu("Komentar post");
    setKontekst();

    KomentarRequestDTO dto = new KomentarRequestDTO("Naslov", "Tekst komentara", false);

    mockMvc.perform(post("/api/prijave/" + id + "/komentari")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tekst").value("Tekst komentara"))
            .andExpect(jsonPath("$.prijavaId").value(id))
            .andExpect(jsonPath("$.korisnikId").value(1));
}

@Test
void dodajKomentar_prazanTekst_vraca400() throws Exception {
    Long id = kreirajPrijavu("Komentar validacija");
    setKontekst();

    KomentarRequestDTO dto = new KomentarRequestDTO("Naslov", "", false);

    mockMvc.perform(post("/api/prijave/" + id + "/komentari")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
}

@Test
void dodajKomentar_nePostojiPrijava_vraca404() throws Exception {
    setKontekst();

    KomentarRequestDTO dto = new KomentarRequestDTO("N", "Tekst", false);

    mockMvc.perform(post("/api/prijave/9999/komentari")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound());
}

// --- POST /{id}/fotografije + GET /{id}/fotografije ----------------------

@Test
void dodajFotografije_uspjesno_vraca201() throws Exception {
    Long id = kreirajPrijavu("Fotografija test");
    setKontekst();

    MockMultipartFile fajl = new MockMultipartFile(
            "fajlovi", "slika.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image-bytes".getBytes());

    mockMvc.perform(multipart("/api/prijave/" + id + "/fotografije")
                    .file(fajl))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].prijavaId").value(id))
            .andExpect(jsonPath("$[0].putanja").exists());
}

@Test
void dohvatiFotografije_uspjesno() throws Exception {
    Long id = kreirajPrijavu("Fotografija dohvat");
    setKontekst();

    MockMultipartFile fajl = new MockMultipartFile(
            "fajlovi", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "bytes".getBytes());
    mockMvc.perform(multipart("/api/prijave/" + id + "/fotografije").file(fajl))
            .andExpect(status().isCreated());

    mockMvc.perform(get("/api/prijave/" + id + "/fotografije"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));
}

@Test
void dodajFotografije_previseSlika_vraca400() throws Exception {
    Long id = kreirajPrijavu("Max fotografija");
    setKontekst();

    // Upload 5 fotografija (maksimum)
    for (int i = 0; i < 5; i++) {
        MockMultipartFile fajl = new MockMultipartFile(
                "fajlovi", "img" + i + ".jpg", MediaType.IMAGE_JPEG_VALUE, ("bytes" + i).getBytes());
        mockMvc.perform(multipart("/api/prijave/" + id + "/fotografije").file(fajl))
                .andExpect(status().isCreated());
    }

    // 6. fotografija treba vratiti 400
    MockMultipartFile extra = new MockMultipartFile(
            "fajlovi", "extra.jpg", MediaType.IMAGE_JPEG_VALUE, "extra".getBytes());
    mockMvc.perform(multipart("/api/prijave/" + id + "/fotografije").file(extra))
            .andExpect(status().isBadRequest());
}

@Test
void dohvatiFotografije_nePostojiPrijava_vraca404() throws Exception {
    setKontekst();

    mockMvc.perform(get("/api/prijave/9999/fotografije"))
            .andExpect(status().isNotFound());
}

// --- POST /{id}/validacija + GET /{id}/validacija ------------------------

@Test
void validiraj_uspjesno() throws Exception {
    Long id = kreirajPrijavu("Validacija test");
    setKontekst();

    ValidacijaRequestDTO dto = new ValidacijaRequestDTO(true);

    mockMvc.perform(post("/api/prijave/" + id + "/validacija")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.prijavaId").value(id))
            .andExpect(jsonPath("$.potvrdjeno").value(true))
            .andExpect(jsonPath("$.korisnikId").value(1));
}

@Test
void validiraj_azuriraPoslijePonovnogGlasanja() throws Exception {
    Long id = kreirajPrijavu("Validacija update");
    setKontekst();

    // Prvo glasanje - potvrda
    mockMvc.perform(post("/api/prijave/" + id + "/validacija")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new ValidacijaRequestDTO(true))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.potvrdjeno").value(true));

    // Drugo glasanje istog korisnika - promjena na osporavanje
    mockMvc.perform(post("/api/prijave/" + id + "/validacija")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new ValidacijaRequestDTO(false))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.potvrdjeno").value(false));
}

@Test
void validiraj_nePostojiPrijava_vraca404() throws Exception {
    setKontekst();

    ValidacijaRequestDTO dto = new ValidacijaRequestDTO(true);

    mockMvc.perform(post("/api/prijave/9999/validacija")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound());
}

@Test
void validacijaStatistika_uspjesno() throws Exception {
    Long id = kreirajPrijavu("Validacija statistika");
    setKontekst();

    // Dodaj jedan glas
    mockMvc.perform(post("/api/prijave/" + id + "/validacija")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new ValidacijaRequestDTO(true))))
            .andExpect(status().isOk());

    mockMvc.perform(get("/api/prijave/" + id + "/validacija"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.prijavaId").value(id))
            .andExpect(jsonPath("$.ukupnoGlasova").value(1))
            .andExpect(jsonPath("$.brPotvrda").value(1))
            .andExpect(jsonPath("$.brOsporavanja").value(0));
}

@Test
void validacijaStatistika_nePostojiPrijava_vraca404() throws Exception {
    setKontekst();

    mockMvc.perform(get("/api/prijave/9999/validacija"))
            .andExpect(status().isNotFound());
}

}