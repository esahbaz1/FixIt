package ba.etf.fixit.managementservice.controller;

import ba.etf.fixit.managementservice.dto.*;
import ba.etf.fixit.managementservice.service.RadnikService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/radnici")
@Tag(name = "Radnici", description = "Upravljanje radnicima gradskih komunalnih službi")
public class RadnikController {

    private final RadnikService service;

    public RadnikController(RadnikService service) { this.service = service; }

    @Operation(summary = "Dohvati sve radnike")
    @ApiResponse(responseCode = "200", description = "Lista radnika")
    @GetMapping
    public ResponseEntity<List<RadnikResponseDTO>> dohvatiSve() {
        return ResponseEntity.ok(service.dohvatiSve());
    }

    @Operation(summary = "Dohvati radnika po ID-u")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Radnik pronađen"),
        @ApiResponse(responseCode = "404", description = "Radnik nije pronađen")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RadnikResponseDTO> dohvatiPoId(
            @Parameter(description = "ID radnika") @PathVariable Long id) {
        return ResponseEntity.ok(service.dohvatiPoId(id));
    }

    @Operation(summary = "Dohvati radnike određene gradske službe")
    @ApiResponse(responseCode = "200", description = "Lista radnika za datu službu")
    @GetMapping("/sluzba/{sluzbaId}")
    public ResponseEntity<List<RadnikResponseDTO>> dohvatiPoSluzbi(
            @Parameter(description = "ID gradske službe") @PathVariable Long sluzbaId) {
        return ResponseEntity.ok(service.dohvatiPoSluzbi(sluzbaId));
    }

    @Operation(summary = "Kreiraj novog radnika",
               description = "Kreira radnika i dodjeljuje ga određenoj gradskoj službi. korisnikId je referenca na korisnika iz user-service.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Radnik uspješno kreiran"),
        @ApiResponse(responseCode = "400", description = "Nevalidni podaci"),
        @ApiResponse(responseCode = "404", description = "Gradska služba sa datim ID-om nije pronađena")
    })
    @PostMapping
    public ResponseEntity<RadnikResponseDTO> kreiraj(@Valid @RequestBody RadnikRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.kreiraj(dto));
    }

    @Operation(summary = "Obriši radnika")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Radnik uspješno obrisan"),
        @ApiResponse(responseCode = "404", description = "Radnik nije pronađen")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> obrisi(
            @Parameter(description = "ID radnika") @PathVariable Long id) {
        service.obrisi(id);
        return ResponseEntity.noContent().build();
    }


@GetMapping("/sluzba/{sluzbaId}/paged")
public ResponseEntity<List<RadnikResponseDTO>> paged(
        @PathVariable Long sluzbaId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size) {

    return ResponseEntity.ok(service.dohvatiPoSluzbiPaged(sluzbaId, page, size));
}


@GetMapping("/sluzba/{sluzbaId}/aktivni")
public ResponseEntity<List<RadnikResponseDTO>> aktivni(
        @PathVariable Long sluzbaId,
        @RequestParam String pozicija) {

    return ResponseEntity.ok(service.aktivniPoPoziciji(sluzbaId, pozicija));
}

@PutMapping("/{radnikId}/premjesti/{sluzbaId}")
public ResponseEntity<RadnikResponseDTO> premjesti(
        @PathVariable Long radnikId,
        @PathVariable Long sluzbaId) {

    return ResponseEntity.ok(service.premjestiRadnika(radnikId, sluzbaId));
}
}
