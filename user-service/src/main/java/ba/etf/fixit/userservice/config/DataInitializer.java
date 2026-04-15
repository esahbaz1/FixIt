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

        korisnikRepository.save(new Korisnik("Amir",  "Hodzic",    "admin@fixit.ba",          "Admin12345!",   UlogaKorisnika.ADMIN));
        korisnikRepository.save(new Korisnik("Mirela","Kovacevic", "mirela@jkp.ba",            "Mirela12345!",  UlogaKorisnika.RUKOVODILAC));
        korisnikRepository.save(new Korisnik("Haris", "Begovic",   "haris@jkp.ba",             "Haris12345!",   UlogaKorisnika.RADNIK));
        korisnikRepository.save(new Korisnik("Emir",  "Music",     "emir@elektro.ba",          "Emir12345!",    UlogaKorisnika.RADNIK));
        korisnikRepository.save(new Korisnik("Lejla", "Mujanovic", "lejla@gmail.com",           "Lejla12345!",   UlogaKorisnika.GRADJANIN));
        korisnikRepository.save(new Korisnik("Damir", "Softic",    "damir@gmail.com",           "Damir12345!",   UlogaKorisnika.GRADJANIN));

        System.out.println(">>> User Service: kreirano 6 korisnika.");
    }
}
