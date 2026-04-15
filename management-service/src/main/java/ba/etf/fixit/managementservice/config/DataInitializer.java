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

        GradskaSluzba jkp    = sluzbaRepo.save(new GradskaSluzba("JKP Sarajevo","Javno komunalno preduzece","info@jkp.ba","033-100-200"));
        GradskaSluzba elektro= sluzbaRepo.save(new GradskaSluzba("Elektrodistribucija BiH","Javna rasvjeta","info@elektro.ba","033-300-400"));
        GradskaSluzba insp   = sluzbaRepo.save(new GradskaSluzba("Komunalna inspekcija","Nadzor komunalnog reda","inspekcija@grad.ba","033-500-600"));
        GradskaSluzba sume   = sluzbaRepo.save(new GradskaSluzba("JP Sarajevske sume","Odrzavanje zelenih povrsina","info@sasume.ba","033-700-800"));
        System.out.println(">>> Kreirane 4 gradske sluzbe.");

        radnikRepo.save(new Radnik(3L, jkp,    "Komunalni radnik", "Put/cesta, Vodovod, Otpad"));
        radnikRepo.save(new Radnik(4L, elektro, "Elektricar",       "Javna rasvjeta, Elektrika"));
        System.out.println(">>> Kreirana 2 radnika.");

        profilRepo.save(new KorisnikProfil(1L,"061-111-111","Sarajevo bb",UlogaKorisnika.ADMIN));
        profilRepo.save(new KorisnikProfil(2L,"061-222-222","Maglaj bb",  UlogaKorisnika.RUKOVODILAC));
        profilRepo.save(new KorisnikProfil(3L,"062-333-333","Tuzla bb",   UlogaKorisnika.RADNIK));
        profilRepo.save(new KorisnikProfil(4L,"062-444-444","Zenica bb",  UlogaKorisnika.RADNIK));
        profilRepo.save(new KorisnikProfil(5L,"063-555-555","Mostar bb",  UlogaKorisnika.GRADJANIN));
        profilRepo.save(new KorisnikProfil(6L,"063-666-666","Bihac bb",   UlogaKorisnika.GRADJANIN));
        System.out.println(">>> Kreirano 6 profila korisnika.");
        System.out.println(">>> Management Service: inicijalizacija zavrsena!");
    }
}
