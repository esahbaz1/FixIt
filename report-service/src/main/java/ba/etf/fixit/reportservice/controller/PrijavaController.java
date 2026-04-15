package ba.etf.fixit.reportservice.controller;

import ba.etf.fixit.reportservice.dto.*;
import ba.etf.fixit.reportservice.repository.PrijavaRepository;
import ba.etf.fixit.reportservice.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prijave")
@Tag(name = "Prijave", description = "Upravljanje prijavama komunalnih problema: kreiranje, praćenje statusa, komentari i dashboard")
public class PrijavaController {

    private final PrijavaService prijavaService;
    private final KomentarService komentarService;
    private final PrijavaRepository prijavaRepo;

    public PrijavaController(PrijavaService prijavaService, KomentarService komentarService,
                              PrijavaRepository prijavaRepo) {
        this.prijavaService = prijavaService;
        this.komentarService = komentarService;
        this.prijavaRepo = prijavaRepo;
    }

    @Operation(summary = "Dohvati sve aktivne prijave",
               description = "Vraća sve prijave koje nisu arhivirane.")
    @ApiResponse(responseCode = "200", description = "Lista prijava")
    @GetMapping
    public ResponseEntity<List<PrijavaResponseDTO>> dohvatiSve() {
        return ResponseEntity.ok(prijavaService.dohvatiSve());
    }

    @Operation(summary = "Dohvati prijavu po ID-u")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prijava pronađena"),
        @ApiResponse(responseCode = "404", description = "Prijava nije pronađena")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PrijavaResponseDTO> dohvatiPoId(
            @Parameter(description = "ID prijave") @PathVariable Long id) {
        return ResponseEntity.ok(prijavaService.dohvatiPoId(id));
    }

    @Operation(summary = "Kreiraj novu prijavu",
               description = "Kreira novu prijavu komunalnog problema. Status se automatski postavlja na 'Novo'.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Prijava uspješno kreirana"),
        @ApiResponse(responseCode = "400", description = "Nevalidni podaci (nedostaje naslov, koordinate, itd.)"),
        @ApiResponse(responseCode = "404", description = "Kategorija sa datim ID-om nije pronađena")
    })
    @PostMapping
    public ResponseEntity<PrijavaResponseDTO> kreiraj(@Valid @RequestBody PrijavaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prijavaService.kreiraj(dto));
    }

    @Operation(summary = "Promijeni status prijave",
               description = "Mijenja status prijave (npr. Novo → U toku → Rijeseno). Parametri se šalju kao query parametri.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status uspješno promijenjen"),
        @ApiResponse(responseCode = "404", description = "Prijava ili status nisu pronađeni")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<PrijavaResponseDTO> promijeniStatus(
            @Parameter(description = "ID prijave") @PathVariable Long id,
            @Parameter(description = "Naziv novog statusa, npr. 'U toku'") @RequestParam String noviStatus,
            @Parameter(description = "ID korisnika koji mijenja status") @RequestParam Long korisnikId) {
        return ResponseEntity.ok(prijavaService.promijeniStatus(id, noviStatus, korisnikId));
    }

    @Operation(summary = "Arhiviraj prijavu",
               description = "Označava prijavu kao arhiviranu. Arhivirane prijave se ne prikazuju u glavnoj listi.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Prijava uspješno arhivirana"),
        @ApiResponse(responseCode = "404", description = "Prijava nije pronađena")
    })
    @PatchMapping("/{id}/arhiviraj")
    public ResponseEntity<Void> arhiviraj(
            @Parameter(description = "ID prijave") @PathVariable Long id) {
        prijavaService.arhiviraj(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Pretraži prijave po ključnoj riječi",
               description = "Pretražuje prijave po naslovu ili opisu.")
    @ApiResponse(responseCode = "200", description = "Lista odgovarajućih prijava")
    @GetMapping("/pretraga")
    public ResponseEntity<List<PrijavaResponseDTO>> pretrazi(
            @Parameter(description = "Ključna riječ za pretragu") @RequestParam String q) {
        return ResponseEntity.ok(
                prijavaRepo.pretraziPoKljucnojRijeci(q).stream()
                        .map(prijavaService::mapToResponse)
                        .collect(Collectors.toList()));
    }

    @Operation(summary = "Dohvati prijave po lokaciji (adresi)")
    @ApiResponse(responseCode = "200", description = "Lista prijava za datu lokaciju")
    @GetMapping("/lokacija")
    public ResponseEntity<List<PrijavaResponseDTO>> poLokaciji(
            @Parameter(description = "Naziv lokacije ili dijela adrese") @RequestParam String lokacija) {
        return ResponseEntity.ok(
                prijavaRepo.findByLokacija(lokacija).stream()
                        .map(prijavaService::mapToResponse)
                        .collect(Collectors.toList()));
    }

    @Operation(summary = "Podaci za heatmapu",
               description = "Vraća koordinate i prioritete svih aktivnih prijava za prikaz na mapi.")
    @ApiResponse(responseCode = "200", description = "Lista tačaka za heatmapu")
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

    @Operation(summary = "Dashboard statistika",
               description = "Vraća ukupan broj prijava, aktivnih prijava i prekoračenih rokova.")
    @ApiResponse(responseCode = "200", description = "Dashboard podaci")
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Map<String, Object> d = new HashMap<>();
        d.put("ukupnoPrijava", prijavaRepo.count());
        d.put("aktivnePrijave", prijavaRepo.findByArhiviranFalse().size());
        d.put("prekoraceniRokovi", prijavaRepo.findPrekoraceniRokovi(LocalDateTime.now()).size());
        return ResponseEntity.ok(d);
    }

    @Operation(summary = "Dohvati javne komentare prijave")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista komentara"),
        @ApiResponse(responseCode = "404", description = "Prijava nije pronađena")
    })
    @GetMapping("/{id}/komentari")
    public ResponseEntity<List<KomentarResponseDTO>> komentari(
            @Parameter(description = "ID prijave") @PathVariable Long id) {
        return ResponseEntity.ok(komentarService.dohvatiJavne(id));
    }

    @Operation(summary = "Dodaj komentar na prijavu")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Komentar uspješno dodan"),
        @ApiResponse(responseCode = "400", description = "Nevalidni podaci"),
        @ApiResponse(responseCode = "404", description = "Prijava nije pronađena")
    })
    @PostMapping("/{id}/komentari")
    public ResponseEntity<KomentarResponseDTO> dodajKomentar(
            @Parameter(description = "ID prijave") @PathVariable Long id,
            @Valid @RequestBody KomentarRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(komentarService.dodaj(id, dto));
    }
}
