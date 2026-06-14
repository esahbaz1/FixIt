package ba.etf.fixit.userservice.controller;

import ba.etf.fixit.userservice.dto.*;
import ba.etf.fixit.userservice.dto.OdjavaRequestDTO;
import ba.etf.fixit.userservice.exception.ForbiddenException;
import ba.etf.fixit.userservice.exception.ResourceNotFoundException;
import ba.etf.fixit.userservice.model.UlogaKorisnika;
import ba.etf.fixit.userservice.service.KorisnikService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class KorisnikController {

    private final KorisnikService korisnikService;

    public KorisnikController(KorisnikService korisnikService) {
        this.korisnikService = korisnikService;
    }

    // --- JAVNI ENDPOINTI -----------------------------------------------------

    @PostMapping("/api/auth/registracija")
    public ResponseEntity<KorisnikResponseDTO> registruj(
            @Valid @RequestBody RegistracijaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(korisnikService.registruj(dto));
    }

    @PostMapping("/api/auth/prijava")
    public ResponseEntity<LoginResponseDTO> prijava(
            @Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(korisnikService.prijava(dto));
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<LoginResponseDTO> osvjeziToken(
            @Valid @RequestBody RefreshTokenRequestDTO dto) {
        return ResponseEntity.ok(korisnikService.osvjeziToken(dto));
    }

    @PostMapping("/api/auth/odjava")
    public ResponseEntity<Void> odjava(
            @Valid @RequestBody OdjavaRequestDTO dto) {
        korisnikService.odjava(dto.getRefreshToken(), dto.getAccessToken());
        return ResponseEntity.noContent().build();
    }

    // --- ZASTICENI ENDPOINTI -------------------------------------------------

    @GetMapping("/api/korisnici")
    public ResponseEntity<List<KorisnikResponseDTO>> dohvatiSve() {
        return ResponseEntity.ok(korisnikService.dohvatiSve());
    }

    @GetMapping("/api/korisnici/{id}")
    public ResponseEntity<KorisnikResponseDTO> dohvatiPoId(@PathVariable Long id) {
        return ResponseEntity.ok(korisnikService.dohvatiPoId(id));
    }

    @DeleteMapping("/api/korisnici/{id}")
    public ResponseEntity<Void> obrisi(@PathVariable Long id, HttpServletRequest request) {
        provjeriUlogu(request, "ADMIN");
        korisnikService.obrisi(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/korisnici/{id}/uloga")
    public ResponseEntity<KorisnikResponseDTO> promijeniUlogu(
            @PathVariable Long id,
            @RequestParam UlogaKorisnika novaUloga,
            HttpServletRequest request) {
        provjeriUlogu(request, "ADMIN");
        return ResponseEntity.ok(korisnikService.promijeniUlogu(id, novaUloga));
    }

    @GetMapping("/api/korisnici/paged")
    public ResponseEntity<List<KorisnikResponseDTO>> dohvatiSvePaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "datumKreiranja") String sortBy) {
        return ResponseEntity.ok(korisnikService.dohvatiSvePaged(page, size, sortBy));
    }

    @PostMapping("/api/korisnici/batch")
    public ResponseEntity<List<KorisnikResponseDTO>> batchRegistracija(
            @RequestBody KorisnikBatchRequestDTO request,
            HttpServletRequest httpRequest) {
        provjeriUlogu(httpRequest, "ADMIN");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(korisnikService.batchRegistracija(request.getKorisnici()));
    }

    @GetMapping("/api/korisnici/aktivni")
    public ResponseEntity<List<KorisnikResponseDTO>> aktivniPoUlozi(
            @RequestParam UlogaKorisnika uloga) {
        return ResponseEntity.ok(korisnikService.aktivniPoUlozi(uloga));
    }

    // --- POMOCNA METODA -------------------------------------------------------

    private void provjeriUlogu(HttpServletRequest request, String... dozvoljeneUloge) {
        String uloga = request.getHeader("X-Korisnik-Uloga");
        if (uloga == null || uloga.isBlank()) {
            throw new ResourceNotFoundException("Korisnik nije autentificiran");
        }
        for (String dozvoljena : dozvoljeneUloge) {
            if (dozvoljena.equals(uloga)) return;
        }
        throw new ForbiddenException(
                "Nedovoljna prava pristupa. Potrebna uloga: " +
                String.join(" ili ", dozvoljeneUloge));
    }
}