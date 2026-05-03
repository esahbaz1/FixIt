package ba.etf.fixit.userservice.config;

import ba.etf.fixit.userservice.model.*;
import ba.etf.fixit.userservice.repository.KorisnikRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final KorisnikRepository korisnikRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(KorisnikRepository korisnikRepository, PasswordEncoder passwordEncoder) {
        this.korisnikRepository = korisnikRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (korisnikRepository.count() > 0) {
            System.out.println(">>> User Service: podaci vec postoje.");
            return;
        }
        System.out.println(">>> User Service: inicijalizacija...");

        korisnikRepository.save(new Korisnik(null, "Amir", "Hodzic", "admin@fixit.ba", passwordEncoder.encode("Admin12345!"), UlogaKorisnika.ADMIN, true, null));
        korisnikRepository.save(new Korisnik(null, "Mirela", "Kovacevic", "mirela@jkp.ba", passwordEncoder.encode("Mirela12345!"), UlogaKorisnika.RUKOVODILAC, true, null));
        korisnikRepository.save(new Korisnik(null, "Haris", "Begovic", "haris@jkp.ba", passwordEncoder.encode("Haris12345!"), UlogaKorisnika.RADNIK, true, null));
        korisnikRepository.save(new Korisnik(null, "Emir", "Music", "emir@elektro.ba", passwordEncoder.encode("Emir12345!"), UlogaKorisnika.RADNIK, true, null));
        korisnikRepository.save(new Korisnik(null, "Lejla", "Mujanovic", "lejla@gmail.com", passwordEncoder.encode("Lejla12345!"), UlogaKorisnika.GRADJANIN, true, null));
        korisnikRepository.save(new Korisnik(null, "Damir", "Softic", "damir@gmail.com", passwordEncoder.encode("Damir12345!"), UlogaKorisnika.GRADJANIN, true, null));

        System.out.println(">>> User Service: kreirano 6 korisnika.");
    }
}
