package ba.etf.fixit.notificationservice.controller;

import ba.etf.fixit.notificationservice.dto.*;
import ba.etf.fixit.notificationservice.model.TipNotifikacije;
import ba.etf.fixit.notificationservice.service.NotifikacijaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/notifikacije")
@Tag(name = "Notifikacije", description = "Upravljanje notifikacijama korisnika: kreiranje, pregled i označavanje kao pročitano")
public class NotifikacijaController {

    private final NotifikacijaService service;

    public NotifikacijaController(NotifikacijaService service) { this.service = service; }

    @Operation(summary = "Dohvati sve notifikacije korisnika",
               description = "Vraća sve notifikacije za datog korisnika, sortirane od najnovije.")
    @ApiResponse(responseCode = "200", description = "Lista notifikacija")
    @GetMapping("/korisnik/{korisnikId}")
    public ResponseEntity<List<NotifikacijaResponseDTO>> dohvatiZaKorisnika(
            @Parameter(description = "ID korisnika") @PathVariable Long korisnikId) {
        return ResponseEntity.ok(service.dohvatiZaKorisnika(korisnikId));
    }

    @Operation(summary = "Dohvati nepročitane notifikacije korisnika")
    @ApiResponse(responseCode = "200", description = "Lista nepročitanih notifikacija")
    @GetMapping("/korisnik/{korisnikId}/neprocitane")
    public ResponseEntity<List<NotifikacijaResponseDTO>> dohvatiNeprocitane(
            @Parameter(description = "ID korisnika") @PathVariable Long korisnikId) {
        return ResponseEntity.ok(service.dohvatiNeprocitane(korisnikId));
    }

    @Operation(summary = "Broj nepročitanih notifikacija korisnika")
    @ApiResponse(responseCode = "200", description = "Broj nepročitanih")
    @GetMapping("/korisnik/{korisnikId}/broj-neprocitanih")
    public ResponseEntity<Map<String, Long>> brojNeprocitanih(
            @Parameter(description = "ID korisnika") @PathVariable Long korisnikId) {
        return ResponseEntity.ok(Map.of("brojNeprocitanih", service.brojNeprocitanih(korisnikId)));
    }

    @Operation(summary = "Kreiraj novu notifikaciju",
               description = "Kreira novu notifikaciju za korisnika. korisnikId i prijavaId su reference na entitete iz других servisa.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Notifikacija uspješno kreirana"),
        @ApiResponse(responseCode = "400", description = "Nevalidni podaci (nedostaje naslov, tip, korisnikId)")
    })
    @PostMapping
    public ResponseEntity<NotifikacijaResponseDTO> kreiraj(@Valid @RequestBody NotifikacijaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.kreiraj(dto));
    }

    @Operation(summary = "Označi notifikaciju kao pročitanu")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notifikacija označena kao pročitana"),
        @ApiResponse(responseCode = "404", description = "Notifikacija nije pronađena")
    })
    @PatchMapping("/{id}/procitano")
    public ResponseEntity<NotifikacijaResponseDTO> oznaci(
            @Parameter(description = "ID notifikacije") @PathVariable Long id) {
        return ResponseEntity.ok(service.oznaciBrojProcitanim(id));
    }


    @GetMapping("/korisnik/{korisnikId}/paged")
public ResponseEntity<List<NotifikacijaResponseDTO>> paged(
        @PathVariable Long korisnikId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size) {

    return ResponseEntity.ok(service.dohvatiZaKorisnikaPaged(korisnikId, page, size));
}



@GetMapping("/korisnik/{korisnikId}/neprocitane-tip")
public ResponseEntity<List<NotifikacijaResponseDTO>> neprocitanePoTipu(
        @PathVariable Long korisnikId,
        @RequestParam TipNotifikacije tip) {

    return ResponseEntity.ok(service.neprocitanePoTipu(korisnikId, tip));
}
}
