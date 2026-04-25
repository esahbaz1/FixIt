package ba.etf.fixit.notificationservice.config;

import ba.etf.fixit.notificationservice.model.*;
import ba.etf.fixit.notificationservice.repository.NotifikacijaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final NotifikacijaRepository notifikacijaRepository;

    public DataInitializer(NotifikacijaRepository notifikacijaRepository) {
        this.notifikacijaRepository = notifikacijaRepository;
    }

    @Override
    public void run(String... args) {
        if (notifikacijaRepository.count() > 0) {
            System.out.println(">>> Notification Service: podaci vec postoje.");
            return;
        }
        System.out.println(">>> Notification Service: inicijalizacija...");

        // korisnikId=5 (Lejla), prijavaId=1 (rupa na putu)
        notifikacijaRepository.save(new Notifikacija(null, 5L, 1L,
                "Vasa prijava je dodijeljena JKP Sarajevo",
                "Prijava 'Velika rupa na asfaltu' je proslijedjena JKP Sarajevo.",
                TipNotifikacije.DODJELA_SLUZBI, false, false, null, null));

        notifikacijaRepository.save(new Notifikacija(null, 5L, 1L,
                "Status prijave promijenjen na U radu",
                "Radnici JKP Sarajevo su izasli na teren.",
                TipNotifikacije.STATUS_PROMJENA, false, false, null, null));

        // korisnikId=1 (Admin), prijavaId=2 (rasvjeta)
        notifikacijaRepository.save(new Notifikacija(null, 1L, 2L,
                "Nova prijava u sistemu - Javna rasvjeta",
                "Korisnik Damir Softic prijavio problem sa rasvjetom na Bascarsiji.",
                TipNotifikacije.NOVA_PRIJAVA, false, false, null, null));

        // korisnikId=5 (Lejla), prijavaId=3 (rijeseno)
        notifikacijaRepository.save(new Notifikacija(null, 5L, 3L,
                "Vas problem je uspjesno rijesen!",
                "Prijava 'Curenje vode - Ilindenska' je rijesena. Cijev zamijenjena.",
                TipNotifikacije.RIJESENO, false, false, null, null));

        // korisnikId=5 (Lejla), prijavaId=1 (novi komentar)
        notifikacijaRepository.save(new Notifikacija(null, 5L, 1L,
                "Novi komentar na vasoj prijavi",
                "JKP Sarajevo je dodao komentar na vasu prijavu.",
                TipNotifikacije.NOVI_KOMENTAR, false, false, null, null));

        System.out.println(">>> Kreirano 5 demo notifikacija.");
        System.out.println(">>> Notification Service: inicijalizacija zavrsena!");
    }
}
