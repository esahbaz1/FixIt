package ba.etf.fixit.userservice.config;

import ba.etf.fixit.userservice.model.*;
import ba.etf.fixit.userservice.repository.KorisnikRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final KorisnikRepository korisnikRepository;

    public DataInitializer(KorisnikRepository korisnikRepository) {
        this.korisnikRepository = korisnikRepository;
    }

    @Override
    public void run(String... args) {
        if (korisnikRepository.count() > 0) {
            System.out.println(">>> User Service: podaci vec postoje.");
            return;
        }
        System.out.println(">>> User Service: inicijalizacija...");

        korisnikRepository.save(new Korisnik(null, "Amir", "Hodzic", "admin@fixit.ba", "Admin12345!", UlogaKorisnika.ADMIN, true, null));
        korisnikRepository.save(new Korisnik(null, "Mirela", "Kovacevic", "mirela@jkp.ba", "Mirela12345!", UlogaKorisnika.RUKOVODILAC, true, null));
        korisnikRepository.save(new Korisnik(null, "Haris", "Begovic", "haris@jkp.ba", "Haris12345!", UlogaKorisnika.RADNIK, true, null));
        korisnikRepository.save(new Korisnik(null, "Emir", "Music", "emir@elektro.ba", "Emir12345!", UlogaKorisnika.RADNIK, true, null));
        korisnikRepository.save(new Korisnik(null, "Lejla", "Mujanovic", "lejla@gmail.com", "Lejla12345!", UlogaKorisnika.GRADJANIN, true, null));
        korisnikRepository.save(new Korisnik(null, "Damir", "Softic", "damir@gmail.com", "Damir12345!", UlogaKorisnika.GRADJANIN, true, null));

        System.out.println(">>> User Service: kreirano 6 korisnika.");
    }
}
