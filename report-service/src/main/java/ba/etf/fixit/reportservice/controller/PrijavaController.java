package ba.etf.fixit.reportservice.controller;

import ba.etf.fixit.reportservice.dto.*;
import ba.etf.fixit.reportservice.repository.PrijavaRepository;
import ba.etf.fixit.reportservice.security.KorisnikKontekst;
import ba.etf.fixit.reportservice.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prijave")
@Tag(name = "Prijave", description = "Upravljanje prijavama komunalnih problema")
public class PrijavaController {

    private final PrijavaService prijavaService;
    private final KomentarService komentarService;
    private final PrijavaRepository prijavaRepo;
    private final ValidacijaService validacijaService;
    private final FotografijaService fotografijaService;
    private final StatistikaService statistikaService;

    public PrijavaController(PrijavaService prijavaService,
                              KomentarService komentarService,
                              PrijavaRepository prijavaRepo,
                              ValidacijaService validacijaService,
                              FotografijaService fotografijaService,
                              StatistikaService statistikaService) {
        this.prijavaService = prijavaService;
        this.komentarService = komentarService;
        this.prijavaRepo = prijavaRepo;
        this.validacijaService = validacijaService;
        this.fotografijaService = fotografijaService;
        this.statistikaService = statistikaService;
    }

    // --- OSNOVNI CRUD --------------------------------------------------------

    @Operation(summary = "Dohvati sve aktivne prijave")
    @GetMapping
    public ResponseEntity<List<PrijavaResponseDTO>> dohvatiSve() {
        return ResponseEntity.ok(prijavaService.dohvatiSve());
    }

    @Operation(summary = "Dohvati prijavu po ID-u")
    @GetMapping("/{id}")
    public ResponseEntity<PrijavaResponseDTO> dohvatiPoId(@PathVariable Long id) {
        return ResponseEntity.ok(prijavaService.dohvatiPoId(id));
    }

    @Operation(summary = "Kreiraj novu prijavu")
    @PostMapping
    public ResponseEntity<Map<String, Object>> kreiraj(@Valid @RequestBody PrijavaRequestDTO dto) {
        Long korisnikId = KorisnikKontekst.korisnikId();
        Map<String, Object> odgovor = prijavaService.kreirajAsync(dto, korisnikId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(odgovor);
    }

    @Operation(summary = "Promijeni status prijave (asinhro?? - pokreće SAGA)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> promijeniStatus(
            @PathVariable Long id,
            @RequestParam String noviStatus) {
        Long korisnikId = KorisnikKontekst.korisnikId();
        Map<String, Object> odgovor = prijavaService.promijeniStatusAsync(id, noviStatus, korisnikId);
        return ResponseEntity.accepted().body(odgovor);
    }

    @Operation(summary = "Parcijalno azuriraj prijavu")
    @PatchMapping("/{id}")
    public ResponseEntity<PrijavaResponseDTO> partialUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> fields) {
        return ResponseEntity.ok(prijavaService.partialUpdate(id, fields));
    }

    @Operation(summary = "Arhiviraj prijavu")
    @PatchMapping("/{id}/arhiviraj")
    public ResponseEntity<Void> arhiviraj(@PathVariable Long id) {
        prijavaService.arhiviraj(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Dodijeli prijavu gradskoj sluzbi",
               description = "Admin dodjeljuje prijavu nadleznoj gradskoj sluzbi.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prijava dodijeljena sluzbi"),
        @ApiResponse(responseCode = "404", description = "Prijava nije pronadjena")
    })
    @PatchMapping("/{id}/dodjeli-sluzbu")
    public ResponseEntity<PrijavaResponseDTO> dodijeliSluzbu(
            @PathVariable Long id,
            @RequestParam Long sluzbaId) {
        return ResponseEntity.ok(prijavaService.dodijeliSluzbu(id, sluzbaId));
    }

    @Operation(summary = "Dodijeli radnika na prijavu",
               description = "Rukovodilac dodjeljuje konkretnog radnika kao odgovorno lice.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Radnik dodijeljen"),
        @ApiResponse(responseCode = "404", description = "Prijava nije pronadjena")
    })
    @PatchMapping("/{id}/dodjeli-radnika")
    public ResponseEntity<PrijavaResponseDTO> dodijeliRadnika(
            @PathVariable Long id,
            @RequestParam Long korisnikId) {
        return ResponseEntity.ok(prijavaService.dodijeliRadnika(id, korisnikId));
    }

    // --- PRETRAGA / FILTERI ---------------------------------------------------

    @GetMapping("/pretraga")
    public ResponseEntity<List<PrijavaResponseDTO>> pretrazi(@RequestParam String q) {
        return ResponseEntity.ok(
                prijavaRepo.pretraziPoKljucnojRijeci(q).stream()
                        .map(prijavaService::mapToResponse)
                        .collect(Collectors.toList()));
    }

    @GetMapping("/lokacija")
    public ResponseEntity<List<PrijavaResponseDTO>> poLokaciji(@RequestParam String lokacija) {
        return ResponseEntity.ok(
                prijavaRepo.findByLokacija(lokacija).stream()
                        .map(prijavaService::mapToResponse)
                        .collect(Collectors.toList()));
    }

    @GetMapping("/hitne-prekoracene")
    public ResponseEntity<List<PrijavaResponseDTO>> hitnePrekoracene() {
        return ResponseEntity.ok(prijavaService.hitneSaPrekoracenimRokom());
    }

    @GetMapping("/paged")
    public ResponseEntity<List<PrijavaResponseDTO>> dohvatiSvePaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "datumPodnosenja") String sortBy) {
        return ResponseEntity.ok(prijavaService.dohvatiSvePaged(page, size, sortBy));
    }

    // --- HEATMAP / DASHBOARD -------------------------------------------------

    @Operation(summary = "Podaci za heatmapu")
    @GetMapping("/heatmap")
    public ResponseEntity<List<Map<String, Object>>> heatmap() {
        List<Map<String, Object>> tacke = prijavaRepo.findZaHeatmap().stream().map(p -> {
            Map<String, Object> t = new HashMap<>();
            t.put("id", p.getId());
            t.put("latitude", p.getLatitude());
            t.put("longitude", p.getLongitude());
            t.put("prioritet", p.getPrioritet());
            t.put("kategorija", p.getKategorija().getNaziv());
            return t;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(tacke);
    }

    @Operation(summary = "Dashboard statistika (osnovna)")
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Map<String, Object> d = new HashMap<>();
        d.put("ukupnoPrijava", prijavaRepo.count());
        d.put("aktivnePrijave", prijavaRepo.findByArhiviranFalse().size());
        d.put("prekoraceniRokovi", prijavaRepo.findPrekoraceniRokovi(LocalDateTime.now()).size());
        return ResponseEntity.ok(d);
    }

    @Operation(summary = "Detaljna statistika i izvjestaji")
    @GetMapping("/statistika")
    public ResponseEntity<StatistikaResponseDTO> statistika() {
        return ResponseEntity.ok(statistikaService.dohvatiStatistiku());
    }

    // --- KOMENTARI -----------------------------------------------------------

    @Operation(summary = "Dohvati javne komentare prijave")
    @GetMapping("/{id}/komentari")
    public ResponseEntity<List<KomentarResponseDTO>> komentari(@PathVariable Long id) {
        return ResponseEntity.ok(komentarService.dohvatiJavne(id));
    }

    @Operation(summary = "Dohvati interne komentare prijave")
    @GetMapping("/{id}/komentari/interni")
    public ResponseEntity<List<KomentarResponseDTO>> interniKomentari(@PathVariable Long id) {
        return ResponseEntity.ok(komentarService.dohvatiInterne(id));
    }

    @Operation(summary = "Dodaj komentar na prijavu")
    @PostMapping("/{id}/komentari")
    public ResponseEntity<KomentarResponseDTO> dodajKomentar(
            @PathVariable Long id,
            @Valid @RequestBody KomentarRequestDTO dto) {
        Long korisnikId = KorisnikKontekst.korisnikId();
        return ResponseEntity.status(HttpStatus.CREATED).body(komentarService.dodaj(id, dto, korisnikId));
    }

    @Operation(summary = "Dohvati historiju (timeline) prijave")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista historije"),
        @ApiResponse(responseCode = "404", description = "Prijava nije pronadjena")
    })
    @GetMapping("/{id}/historija")
    public ResponseEntity<List<HistorijaResponseDTO>> historija(@PathVariable Long id) {
        return ResponseEntity.ok(komentarService.dohvatiHistoriju(id));
    }

    @Operation(summary = "Dodaj fotografije na prijavu")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Fotografije uspjesno dodane"),
        @ApiResponse(responseCode = "400", description = "Previsen broj fotografija ili nevalidan fajl"),
        @ApiResponse(responseCode = "404", description = "Prijava nije pronadjena")
    })
    @PostMapping(value = "/{id}/fotografije", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<FotografijaResponseDTO>> dodajFotografije(
            @PathVariable Long id,
            @RequestParam("fajlovi") MultipartFile[] fajlovi) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fotografijaService.dodajFotografije(id, fajlovi));
    }

    @Operation(summary = "Dohvati fotografije prijave")
    @GetMapping("/{id}/fotografije")
    public ResponseEntity<List<FotografijaResponseDTO>> dohvatiFotografije(@PathVariable Long id) {
        return ResponseEntity.ok(fotografijaService.dohvatiFotografije(id));
    }

    @Operation(summary = "Validiraj prijavu (glasanje zajednice)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Glas uspjesno zabiljezan"),
        @ApiResponse(responseCode = "404", description = "Prijava nije pronadjena")
    })
    @PostMapping("/{id}/validacija")
    public ResponseEntity<ValidacijaResponseDTO> validiraj(
            @PathVariable Long id,
            @Valid @RequestBody ValidacijaRequestDTO dto) {
        Long korisnikId = KorisnikKontekst.korisnikId();
        return ResponseEntity.ok(validacijaService.validiraj(id, dto, korisnikId));
    }

    @Operation(summary = "Dohvati statistiku glasanja za prijavu")
    @GetMapping("/{id}/validacija")
    public ResponseEntity<ValidacijaStatistikaDTO> validacijaStatistika(@PathVariable Long id) {
        return ResponseEntity.ok(validacijaService.dohvatiStatistiku(id));
    }
}
