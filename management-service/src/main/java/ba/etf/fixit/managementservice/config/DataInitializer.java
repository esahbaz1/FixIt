package ba.etf.fixit.managementservice.config;
import ba.etf.fixit.managementservice.model.*;
import ba.etf.fixit.managementservice.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final GradskaSluzbaRepository sluzbaRepo;
    private final RadnikRepository radnikRepo;
    private final KorisnikProfilRepository profilRepo;

    public DataInitializer(GradskaSluzbaRepository sluzbaRepo, RadnikRepository radnikRepo, KorisnikProfilRepository profilRepo){
        this.sluzbaRepo=sluzbaRepo; this.radnikRepo=radnikRepo; this.profilRepo=profilRepo;
    }

    @Override
    public void run(String... args){
        if(sluzbaRepo.count()>0){ System.out.println(">>> Management Service: podaci vec postoje."); return; }
        System.out.println(">>> Management Service: inicijalizacija...");

        GradskaSluzba jkp    = sluzbaRepo.save(new GradskaSluzba(null, "JKP Sarajevo", "Javno komunalno preduzece", "info@jkp.ba", "033-100-200", true));
        GradskaSluzba elektro= sluzbaRepo.save(new GradskaSluzba(null, "Elektrodistribucija BiH", "Javna rasvjeta", "info@elektro.ba", "033-300-400", true));
        GradskaSluzba insp   = sluzbaRepo.save(new GradskaSluzba(null, "Komunalna inspekcija", "Nadzor komunalnog reda", "inspekcija@grad.ba", "033-500-600", true));
        GradskaSluzba sume   = sluzbaRepo.save(new GradskaSluzba(null, "JP Sarajevske sume", "Odrzavanje zelenih povrsina", "info@sasume.ba", "033-700-800", true));
        System.out.println(">>> Kreirane 4 gradske sluzbe.");

        radnikRepo.save(new Radnik(null, 3L, jkp, "Komunalni radnik", "Put/cesta, Vodovod, Otpad", true));
        radnikRepo.save(new Radnik(null, 4L, elektro, "Elektricar", "Javna rasvjeta, Elektrika", true));
        System.out.println(">>> Kreirana 2 radnika.");

        profilRepo.save(new KorisnikProfil(null, 1L, "061-111-111", "Sarajevo bb", UlogaKorisnika.ADMIN, true));
        profilRepo.save(new KorisnikProfil(null, 2L, "061-222-222", "Maglaj bb", UlogaKorisnika.RUKOVODILAC, true));
        profilRepo.save(new KorisnikProfil(null, 3L, "062-333-333", "Tuzla bb", UlogaKorisnika.RADNIK, true));
        profilRepo.save(new KorisnikProfil(null, 4L, "062-444-444", "Zenica bb", UlogaKorisnika.RADNIK, true));
        profilRepo.save(new KorisnikProfil(null, 5L, "063-555-555", "Mostar bb", UlogaKorisnika.GRADJANIN, true));
        profilRepo.save(new KorisnikProfil(null, 6L, "063-666-666", "Bihac bb", UlogaKorisnika.GRADJANIN, true));
        System.out.println(">>> Kreirano 6 profila korisnika.");
        System.out.println(">>> Management Service: inicijalizacija zavrsena!");
    }
}
