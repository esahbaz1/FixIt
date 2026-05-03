package ba.etf.fixit.userservice.service;

import ba.etf.fixit.userservice.dto.*;
import ba.etf.fixit.userservice.exception.*;
import ba.etf.fixit.userservice.model.*;
import ba.etf.fixit.userservice.repository.KorisnikRepository;
import ba.etf.fixit.userservice.security.JwtServis;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class KorisnikService {

    private final KorisnikRepository korisnikRepository;
    private final JwtServis jwtServis;
    private final PasswordEncoder passwordEncoder;

    public KorisnikService(KorisnikRepository korisnikRepository,
                           JwtServis jwtServis,
                           PasswordEncoder passwordEncoder) {
        this.korisnikRepository = korisnikRepository;
        this.jwtServis = jwtServis;
        this.passwordEncoder = passwordEncoder;
    }

    public List<KorisnikResponseDTO> dohvatiSve() {
        return korisnikRepository.findAll().stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public KorisnikResponseDTO dohvatiPoId(Long id) {
        return mapToResponse(nadjiKorisnika(id));
    }

    /**
     * Uloga se ne prima iz zahtjeva — uvijek je GRADJANIN.
     */
    public KorisnikResponseDTO registruj(RegistracijaRequestDTO dto) {
        if (korisnikRepository.existsByEmail(dto.getEmail())) {
            throw new DuplikatException("Korisnik sa emailom '" + dto.getEmail() + "' vec postoji");
        }
        Korisnik k = new Korisnik(null, dto.getIme(), dto.getPrezime(), dto.getEmail(),
                passwordEncoder.encode(dto.getLozinka()), UlogaKorisnika.GRADJANIN, true, null);
        return mapToResponse(korisnikRepository.save(k));
    }

    public LoginResponseDTO prijava(LoginRequestDTO dto) {
        Korisnik k = korisnikRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Pogresni podaci za prijavu"));
        if (!passwordEncoder.matches(dto.getLozinka(), k.getLozinka())) {
            throw new ResourceNotFoundException("Pogresni podaci za prijavu");
        }
        if (!k.getAktivan()) {
            throw new ResourceNotFoundException("Korisnik je deaktiviran");
        }
        return new LoginResponseDTO(k.getId(), k.getEmail(), k.getIme(), k.getPrezime(),
                k.getUloga(), "Prijava uspjesna",
                jwtServis.kreirajToken(k),
                jwtServis.kreirajRefreshToken(k));
    }

    public LoginResponseDTO osvjeziToken(RefreshTokenRequestDTO dto) {
        if (!jwtServis.jeValidanRefreshToken(dto.getRefreshToken())) {
            throw new ResourceNotFoundException(
                    "Refresh token nije valjan ili je istekao. Potrebna ponovna prijava.");
        }
        String email = jwtServis.dohvatiEmail(dto.getRefreshToken());
        Korisnik k = korisnikRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik nije pronadjen"));
        if (!k.getAktivan()) {
            throw new ResourceNotFoundException("Korisnik je deaktiviran");
        }
        return new LoginResponseDTO(k.getId(), k.getEmail(), k.getIme(), k.getPrezime(),
                k.getUloga(), "Token osvjezen",
                jwtServis.kreirajToken(k),
                dto.getRefreshToken());
    }

    public void odjava(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            jwtServis.invalidisiRefreshToken(refreshToken);
        }
    }

    public KorisnikResponseDTO promijeniUlogu(Long id, UlogaKorisnika novaUloga) {
        Korisnik k = nadjiKorisnika(id);
        k.setUloga(novaUloga);
        return mapToResponse(korisnikRepository.save(k));
    }

    public List<KorisnikResponseDTO> aktivniPoUlozi(UlogaKorisnika uloga) {
        return korisnikRepository.findByUlogaAndAktivanTrue(uloga)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public void obrisi(Long id) {
        if (!korisnikRepository.existsById(id))
            throw new ResourceNotFoundException("Korisnik sa ID-em " + id + " nije pronadjen");
        korisnikRepository.deleteById(id);
    }

    public List<KorisnikResponseDTO> dohvatiSvePaged(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return korisnikRepository.findAll(pageable)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<KorisnikResponseDTO> batchRegistracija(List<RegistracijaRequestDTO> lista) {
        List<Korisnik> korisnici = lista.stream().map(dto -> {
            if (korisnikRepository.existsByEmail(dto.getEmail()))
                throw new DuplikatException("Email vec postoji: " + dto.getEmail());
            return new Korisnik(null, dto.getIme(), dto.getPrezime(), dto.getEmail(),
                    passwordEncoder.encode(dto.getLozinka()), UlogaKorisnika.GRADJANIN, true, null);
        }).collect(Collectors.toList());
        return korisnikRepository.saveAll(korisnici)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public KorisnikResponseDTO mapToResponse(Korisnik k) {
        return new KorisnikResponseDTO(k.getId(), k.getIme(), k.getPrezime(),
                k.getEmail(), k.getUloga(), k.getAktivan(), k.getDatumKreiranja());
    }

    private Korisnik nadjiKorisnika(Long id) {
        return korisnikRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Korisnik sa ID-em " + id + " nije pronadjen"));
    }
}