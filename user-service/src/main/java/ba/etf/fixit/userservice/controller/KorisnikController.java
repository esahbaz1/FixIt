package ba.etf.fixit.userservice.controller;

import ba.etf.fixit.userservice.dto.*;
import ba.etf.fixit.userservice.service.KorisnikService;
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
@Tag(name = "Korisnici", description = "Upravljanje korisnicima: registracija, autentifikacija i pregled profila")
public class KorisnikController {

    private final KorisnikService korisnikService;

    public KorisnikController(KorisnikService korisnikService) {
        this.korisnikService = korisnikService;
    }

    @Operation(summary = "Registracija novog korisnika",
               description = "Kreira novi korisnički račun. Email mora biti jedinstven u sistemu.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Korisnik uspješno registrovan"),
        @ApiResponse(responseCode = "400", description = "Nevalidni podaci (neispravni format emaila, kratka lozinka, itd.)",
                     content = @Content(schema = @Schema(example = "{\"greska\":\"VALIDATION_ERROR\",\"poruke\":{\"email\":\"mora biti validan email\"}}"))),
        @ApiResponse(responseCode = "409", description = "Korisnik sa tim emailom već postoji",
                     content = @Content(schema = @Schema(example = "{\"greska\":\"CONFLICT\",\"poruka\":\"Korisnik sa emailom 'x@y.ba' vec postoji\"}")))
    })
    @PostMapping("/api/auth/registracija")
    public ResponseEntity<KorisnikResponseDTO> registruj(@Valid @RequestBody RegistracijaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(korisnikService.registruj(dto));
    }

    @Operation(summary = "Prijava korisnika u sistem",
               description = "Autentifikacija korisnika emailom i lozinkom. Vraća osnovne podatke o korisniku.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prijava uspješna"),
        @ApiResponse(responseCode = "400", description = "Nevalidni podaci"),
        @ApiResponse(responseCode = "404", description = "Pogrešan email ili lozinka")
    })
    @PostMapping("/api/auth/prijava")
    public ResponseEntity<LoginResponseDTO> prijava(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(korisnikService.prijava(dto));
    }

    @Operation(summary = "Dohvati sve korisnike",
               description = "Vraća listu svih registrovanih korisnika bez lozinki.")
    @ApiResponse(responseCode = "200", description = "Lista korisnika")
    @GetMapping("/api/korisnici")
    public ResponseEntity<List<KorisnikResponseDTO>> dohvatiSve() {
        return ResponseEntity.ok(korisnikService.dohvatiSve());
    }

    @Operation(summary = "Dohvati korisnika po ID-u")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Korisnik pronađen"),
        @ApiResponse(responseCode = "404", description = "Korisnik nije pronađen")
    })
    @GetMapping("/api/korisnici/{id}")
    public ResponseEntity<KorisnikResponseDTO> dohvatiPoId(
            @Parameter(description = "ID korisnika") @PathVariable Long id) {
        return ResponseEntity.ok(korisnikService.dohvatiPoId(id));
    }

    @Operation(summary = "Obriši korisnika po ID-u")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Korisnik uspješno obrisan"),
        @ApiResponse(responseCode = "404", description = "Korisnik nije pronađen")
    })
    @DeleteMapping("/api/korisnici/{id}")
    public ResponseEntity<Void> obrisi(
            @Parameter(description = "ID korisnika") @PathVariable Long id) {
        korisnikService.obrisi(id);
        return ResponseEntity.noContent().build();
    }
}
