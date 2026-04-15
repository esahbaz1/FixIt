package ba.etf.fixit.managementservice.controller;

import ba.etf.fixit.managementservice.dto.*;
import ba.etf.fixit.managementservice.service.GradskaSluzbaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/gradske-sluzbe")
@Tag(name = "Gradske Službe", description = "Upravljanje gradskim komunalnim službama")
public class GradskaSluzbaController {

    private final GradskaSluzbaService service;

    public GradskaSluzbaController(GradskaSluzbaService service) { this.service = service; }

    @Operation(summary = "Dohvati sve gradske službe")
    @ApiResponse(responseCode = "200", description = "Lista gradskih službi")
    @GetMapping
    public ResponseEntity<List<GradskaSluzbaResponseDTO>> dohvatiSve() {
        return ResponseEntity.ok(service.dohvatiSve());
    }

    @Operation(summary = "Dohvati gradsku službu po ID-u")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Gradska služba pronađena"),
        @ApiResponse(responseCode = "404", description = "Gradska služba nije pronađena")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GradskaSluzbaResponseDTO> dohvatiPoId(
            @Parameter(description = "ID gradske službe") @PathVariable Long id) {
        return ResponseEntity.ok(service.dohvatiPoId(id));
    }

    @Operation(summary = "Kreiraj novu gradsku službu",
               description = "Naziv gradske službe mora biti jedinstven u sistemu.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Gradska služba uspješno kreirana"),
        @ApiResponse(responseCode = "400", description = "Nevalidni podaci",
                     content = @Content(schema = @Schema(example = "{\"greska\":\"VALIDATION_ERROR\",\"poruke\":{\"naziv\":\"Naziv sluzbe ne smije biti prazan\"}}"))),
        @ApiResponse(responseCode = "409", description = "Služba sa tim nazivom već postoji")
    })
    @PostMapping
    public ResponseEntity<GradskaSluzbaResponseDTO> kreiraj(@Valid @RequestBody GradskaSluzbaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.kreiraj(dto));
    }

    @Operation(summary = "Ažuriraj gradsku službu")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Gradska služba uspješno ažurirana"),
        @ApiResponse(responseCode = "404", description = "Gradska služba nije pronađena")
    })
    @PutMapping("/{id}")
    public ResponseEntity<GradskaSluzbaResponseDTO> azuriraj(
            @Parameter(description = "ID gradske službe") @PathVariable Long id,
            @Valid @RequestBody GradskaSluzbaRequestDTO dto) {
        return ResponseEntity.ok(service.azuriraj(id, dto));
    }

    @Operation(summary = "Obriši gradsku službu")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Gradska služba uspješno obrisana"),
        @ApiResponse(responseCode = "404", description = "Gradska služba nije pronađena")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> obrisi(
            @Parameter(description = "ID gradske službe") @PathVariable Long id) {
        service.obrisi(id);
        return ResponseEntity.noContent().build();
    }
}
