package ba.etf.fixit.managementservice.controller;

import ba.etf.fixit.managementservice.client.ReportServiceKlijent;
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
@Tag(name = "Radnici", description = "Upravljanje radnicima gradskih komunalnih sluzbi")
public class RadnikController {

    private final RadnikService service;
    private final ReportServiceKlijent reportServiceKlijent;

    public RadnikController(RadnikService service, ReportServiceKlijent reportServiceKlijent) {
        this.service = service;
        this.reportServiceKlijent = reportServiceKlijent;
    }

    @Operation(summary = "Dohvati sve radnike")
    @GetMapping
    public ResponseEntity<List<RadnikResponseDTO>> dohvatiSve() {
        return ResponseEntity.ok(service.dohvatiSve());
    }

    @Operation(summary = "Dohvati radnika po ID-u")
    @GetMapping("/{id}")
    public ResponseEntity<RadnikResponseDTO> dohvatiPoId(@PathVariable Long id) {
        return ResponseEntity.ok(service.dohvatiPoId(id));
    }

    @Operation(summary = "Dohvati radnike odredjene gradske sluzbe")
    @GetMapping("/sluzba/{sluzbaId}")
    public ResponseEntity<List<RadnikResponseDTO>> dohvatiPoSluzbi(@PathVariable Long sluzbaId) {
        return ResponseEntity.ok(service.dohvatiPoSluzbi(sluzbaId));
    }

    @Operation(summary = "Dohvati radnika po korisnikId (veza sa user-service)")
    @GetMapping("/korisnik/{korisnikId}")
    public ResponseEntity<RadnikResponseDTO> dohvatiPoKorisniku(@PathVariable Long korisnikId) {
        return ResponseEntity.ok(service.dohvatiPoKorisnikuId(korisnikId));
    }

    @Operation(summary = "Kreiraj novog radnika")
    @PostMapping
    public ResponseEntity<RadnikResponseDTO> kreiraj(@Valid @RequestBody RadnikRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.kreiraj(dto));
    }

    @Operation(summary = "Obrisi radnika")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> obrisi(@PathVariable Long id) {
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

    @Operation(summary = "Dodijeli radnika na konkretnu prijavu",
               description = "Rukovodilac dodjeljuje radnika (po ID-u radnika) kao odgovorno lice za rjesavanje prijave. " +
                             "Poziva report-service da azurira odgovornoLiceId na prijavi.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Radnik uspjesno dodijeljen prijavi"),
        @ApiResponse(responseCode = "404", description = "Radnik ili prijava nisu pronadjeni"),
        @ApiResponse(responseCode = "503", description = "Report servis nije dostupan")
    })
    @PostMapping("/{radnikId}/prijave/{prijavaId}")
    public ResponseEntity<Void> dodijeliNaPrijavu(
            @Parameter(description = "ID radnika u management-service") @PathVariable Long radnikId,
            @Parameter(description = "ID prijave u report-service") @PathVariable Long prijavaId) {
        RadnikResponseDTO radnik = service.dohvatiPoId(radnikId);
        reportServiceKlijent.dodijeliRadnikaNaPrijavu(prijavaId, radnik.getKorisnikId());
        return ResponseEntity.noContent().build();
    }
}