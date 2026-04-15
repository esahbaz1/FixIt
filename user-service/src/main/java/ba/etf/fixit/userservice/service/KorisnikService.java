package ba.etf.fixit.userservice.service;

import ba.etf.fixit.userservice.dto.*;
import ba.etf.fixit.userservice.exception.*;
import ba.etf.fixit.userservice.model.*;
import ba.etf.fixit.userservice.repository.KorisnikRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class KorisnikService {

    private final KorisnikRepository korisnikRepository;

    public KorisnikService(KorisnikRepository korisnikRepository) {
        this.korisnikRepository = korisnikRepository;
    }

    public List<KorisnikResponseDTO> dohvatiSve() {
        return korisnikRepository.findAll().stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public KorisnikResponseDTO dohvatiPoId(Long id) {
        return mapToResponse(nadjiKorisnika(id));
    }

    public KorisnikResponseDTO registruj(RegistracijaRequestDTO dto) {
        if (korisnikRepository.existsByEmail(dto.getEmail())) {
            throw new DuplikatException("Korisnik sa emailom '" + dto.getEmail() + "' vec postoji");
        }
        UlogaKorisnika uloga = dto.getUloga() != null ? dto.getUloga() : UlogaKorisnika.GRADJANIN;
        Korisnik k = new Korisnik(dto.getIme(), dto.getPrezime(), dto.getEmail(), dto.getLozinka(), uloga);
        return mapToResponse(korisnikRepository.save(k));
    }

    public LoginResponseDTO prijava(LoginRequestDTO dto) {
        Korisnik k = korisnikRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa tim emailom ne postoji"));
        if (!k.getLozinka().equals(dto.getLozinka())) {
            throw new ResourceNotFoundException("Pogresna lozinka");
        }
        if (!k.getAktivan()) {
            throw new ResourceNotFoundException("Korisnik je deaktiviran");
        }
        return new LoginResponseDTO(k.getId(), k.getEmail(), k.getIme(), k.getPrezime(),
                k.getUloga(), "Prijava uspjesna");
    }

    public void obrisi(Long id) {
        if (!korisnikRepository.existsById(id))
            throw new ResourceNotFoundException("Korisnik sa ID-em " + id + " nije pronadjen");
        korisnikRepository.deleteById(id);
    }

    private Korisnik nadjiKorisnika(Long id) {
        return korisnikRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa ID-em " + id + " nije pronadjen"));
    }

    public KorisnikResponseDTO mapToResponse(Korisnik k) {
        return new KorisnikResponseDTO(k.getId(), k.getIme(), k.getPrezime(),
                k.getEmail(), k.getUloga(), k.getAktivan(), k.getDatumKreiranja());
    }
}
