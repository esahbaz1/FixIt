package ba.etf.fixit.userservice.controller;

import ba.etf.fixit.userservice.dto.KorisnikBatchRequestDTO;
import ba.etf.fixit.userservice.dto.LoginRequestDTO;
import ba.etf.fixit.userservice.dto.RegistracijaRequestDTO;
import ba.etf.fixit.userservice.model.UlogaKorisnika;
import ba.etf.fixit.userservice.repository.KorisnikRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class KorisnikControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private KorisnikRepository korisnikRepository;

    @BeforeEach
    void setUp() {
        korisnikRepository.deleteAll();
    }

    private RegistracijaRequestDTO validRegistracija(String email, UlogaKorisnika uloga) {
        RegistracijaRequestDTO dto = new RegistracijaRequestDTO();
        dto.setIme("Ana");
        dto.setPrezime("Simic");
        dto.setEmail(email);
        dto.setLozinka("Lozinka123!");
        dto.setUloga(uloga);
        return dto;
    }

    @Test
    void registracija_uspjesno() throws Exception {
        RegistracijaRequestDTO dto = validRegistracija("ana@test.ba", UlogaKorisnika.GRADJANIN);

        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@test.ba"))
                .andExpect(jsonPath("$.lozinka").doesNotExist());
    }

    @Test
    void registracija_nevalidanEmail_vraca400() throws Exception {
        RegistracijaRequestDTO dto = validRegistracija("NIJE_EMAIL", UlogaKorisnika.GRADJANIN);

        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greska").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.poruke.email").exists());
    }

    @Test
    void registracija_kratkaLozinka_vraca400() throws Exception {
        RegistracijaRequestDTO dto = validRegistracija("ana3@test.ba", UlogaKorisnika.GRADJANIN);
        dto.setLozinka("123");

        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greska").value("VALIDATION_ERROR"));
    }

    @Test
    void registracija_duplikatEmail_vraca409() throws Exception {
        RegistracijaRequestDTO dto = validRegistracija("duplikat@test.ba", UlogaKorisnika.GRADJANIN);

        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.greska").value("CONFLICT"));
    }

    @Test
    void prijava_uspjesno() throws Exception {
        RegistracijaRequestDTO reg = validRegistracija("ana2@test.ba", UlogaKorisnika.GRADJANIN);
        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequestDTO login = new LoginRequestDTO();
        login.setEmail("ana2@test.ba");
        login.setLozinka("Lozinka123!");

        mockMvc.perform(post("/api/auth/prijava")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.poruka").value("Prijava uspjesna"));
    }

    @Test
    void prijava_pogresnaLozinka_vraca404() throws Exception {
        RegistracijaRequestDTO reg = validRegistracija("ana4@test.ba", UlogaKorisnika.GRADJANIN);
        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequestDTO login = new LoginRequestDTO();
        login.setEmail("ana4@test.ba");
        login.setLozinka("Pogresna123!");

        mockMvc.perform(post("/api/auth/prijava")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void dohvatiSve_uspjesno() throws Exception {
        mockMvc.perform(get("/api/korisnici"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void dohvatiPoId_uspjesno() throws Exception {
        RegistracijaRequestDTO dto = validRegistracija("id@test.ba", UlogaKorisnika.GRADJANIN);
        String response = mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/korisnici/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("id@test.ba"));
    }

    @Test
    void dohvatiPoId_nePostoji_vraca404() throws Exception {
        mockMvc.perform(get("/api/korisnici/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void obrisi_uspjesno() throws Exception {
        RegistracijaRequestDTO dto = validRegistracija("delete@test.ba", UlogaKorisnika.GRADJANIN);
        String response = mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/korisnici/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/korisnici/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void obrisi_nePostoji_vraca404() throws Exception {
        mockMvc.perform(delete("/api/korisnici/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.greska").value("NOT_FOUND"));
    }

    @Test
    void dohvatiSvePaged_uspjesno() throws Exception {
        mockMvc.perform(get("/api/korisnici/paged")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "datumKreiranja"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void dohvatiSvePaged_nevalidanPage_vraca500() throws Exception {
        mockMvc.perform(get("/api/korisnici/paged")
                        .param("page", "abc"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.greska").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    void batchRegistracija_uspjesno() throws Exception {
        KorisnikBatchRequestDTO request = new KorisnikBatchRequestDTO();
        request.setKorisnici(List.of(
                validRegistracija("batch1@test.ba", UlogaKorisnika.GRADJANIN),
                validRegistracija("batch2@test.ba", UlogaKorisnika.RADNIK)
        ));

        mockMvc.perform(post("/api/korisnici/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void batchRegistracija_duplikat_vraca409() throws Exception {
        KorisnikBatchRequestDTO request = new KorisnikBatchRequestDTO();
        request.setKorisnici(List.of(validRegistracija("dupli-batch@test.ba", UlogaKorisnika.GRADJANIN)));

        mockMvc.perform(post("/api/korisnici/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/korisnici/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.greska").value("CONFLICT"));
    }

    @Test
    void aktivniPoUlozi_uspjesno() throws Exception {
        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRegistracija("aktivni@test.ba", UlogaKorisnika.RADNIK))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/korisnici/aktivni").param("uloga", "RADNIK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].uloga").value("RADNIK"));
    }

    @Test
    void aktivniPoUlozi_nevalidnaUloga_vraca500() throws Exception {
        mockMvc.perform(get("/api/korisnici/aktivni").param("uloga", "NEPOSTOJECA"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.greska").value("INTERNAL_SERVER_ERROR"));
    }
}
