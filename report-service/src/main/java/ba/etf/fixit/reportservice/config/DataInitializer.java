package ba.etf.fixit.reportservice.config;

import ba.etf.fixit.reportservice.model.*;
import ba.etf.fixit.reportservice.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final StatusiRepository statusiRepo;
    private final KategorijaRepository kategorijaRepo;
    private final PrijavaRepository prijavaRepo;
    private final FotografijaRepository fotografijaRepo;
    private final KomentarRepository komentarRepo;
    private final TipPromjeneRepository tipRepo;
    private final HistorijaPrijaveRepository historijaRepo;
    private final ValidacijaRepository validacijaRepo;
    private final ArhivaRepository arhivaRepo;

    public DataInitializer(StatusiRepository statusiRepo, KategorijaRepository kategorijaRepo,
                           PrijavaRepository prijavaRepo, FotografijaRepository fotografijaRepo,
                           KomentarRepository komentarRepo, TipPromjeneRepository tipRepo,
                           HistorijaPrijaveRepository historijaRepo, ValidacijaRepository validacijaRepo,
                           ArhivaRepository arhivaRepo) {
        this.statusiRepo = statusiRepo; this.kategorijaRepo = kategorijaRepo;
        this.prijavaRepo = prijavaRepo; this.fotografijaRepo = fotografijaRepo;
        this.komentarRepo = komentarRepo; this.tipRepo = tipRepo;
        this.historijaRepo = historijaRepo; this.validacijaRepo = validacijaRepo;
        this.arhivaRepo = arhivaRepo;
    }

    @Override
    public void run(String... args) {
        if (statusiRepo.count() > 0) {
            System.out.println(">>> Report Service: podaci vec postoje.");
            return;
        }
        System.out.println(">>> Report Service: inicijalizacija...");

        // Statusi
        Statusi sNovo        = statusiRepo.save(new Statusi("Novo",        "Novoprijavljen problem"));
        Statusi sDodijeljeno = statusiRepo.save(new Statusi("Dodijeljeno", "Dodijeljen nadleznoj sluzbi"));
        Statusi sURadu       = statusiRepo.save(new Statusi("U radu",      "Radnici rade na rjesavanju"));
        Statusi sRijeseno    = statusiRepo.save(new Statusi("Rijeseno",    "Problem uspjesno rijesen"));
        Statusi sZatvoreno   = statusiRepo.save(new Statusi("Zatvoreno",   "Prijava zatvorena"));
        System.out.println(">>> Kreirano 5 statusa.");

        // Tipovi promjena
        TipPromjene tp1 = tipRepo.save(new TipPromjene(null,          "Novo"));
        TipPromjene tp2 = tipRepo.save(new TipPromjene("Novo",        "Dodijeljeno"));
        TipPromjene tp3 = tipRepo.save(new TipPromjene("Dodijeljeno", "U radu"));
        TipPromjene tp4 = tipRepo.save(new TipPromjene("U radu",      "Rijeseno"));
        TipPromjene tp5 = tipRepo.save(new TipPromjene("Rijeseno",    "Zatvoreno"));
        TipPromjene tp6 = tipRepo.save(new TipPromjene("Zatvoreno",   "Novo"));
        System.out.println(">>> Kreirano 6 tipova promjena.");

        // Kategorije
        Kategorija putCesta  = kategorijaRepo.save(new Kategorija("Put/cesta",      "Ostecenja kolovoza", 1L));
        Kategorija rasvjeta  = kategorijaRepo.save(new Kategorija("Javna rasvjeta", "Pokvarena rasvjeta", 2L));
        Kategorija vodovod   = kategorijaRepo.save(new Kategorija("Vodovod",        "Kvarovi vodovodne mreze", 1L));
        Kategorija zelenilo  = kategorijaRepo.save(new Kategorija("Zelenilo",       "Odrzavanje parkova", 4L));
        Kategorija otpad     = kategorijaRepo.save(new Kategorija("Otpad",          "Nelegalne deponije", 1L));
        Kategorija saobracaj = kategorijaRepo.save(new Kategorija("Saobracaj",      "Semafori i znakovi", 3L));
        Kategorija ostalo    = kategorijaRepo.save(new Kategorija("Ostalo",         "Ostali problemi",    1L));
        System.out.println(">>> Kreirano 7 kategorija.");

        // Prijava 1 - rupa na putu (U radu)
        Prijava p1 = new Prijava("Velika rupa na asfaltu - Titova ulica",
                "Na raskrsnici Titove i Marsala Tita postoji rupa oko 30cm dubine.",
                43.8563, 18.4131, "Titova ulica bb, Sarajevo", putCesta, 5L, sURadu);
        p1.setPrioritet(PrioritetPrijave.VISOK);
        p1.setGrdSluzbald(1L);
        prijavaRepo.save(p1);
        fotografijaRepo.save(new Fotografija(p1, "/uploads/prijave/1/rupa-titova.jpg"));
        komentarRepo.save(new Komentar(5L, p1, "Hitna intervencija", "Molim hitnu intervenciju!", false));
        komentarRepo.save(new Komentar(2L, p1, "Interna napomena", "Dodijeljen radniku.", true));
        historijaRepo.save(new HistorijaPrijave(tp1, p1, 5L));
        historijaRepo.save(new HistorijaPrijave(tp2, p1, 1L));
        historijaRepo.save(new HistorijaPrijave(tp3, p1, 2L));
        validacijaRepo.save(new Validacija(p1, 6L, true));

        // Prijava 2 - rasvjeta (Novo)
        Prijava p2 = new Prijava("Pokvarena ulicna svjetla - Bascarsija",
                "Na Bascarsiji ne rade 3 ulicna svjetla vec sedmicu.",
                43.8601, 18.4311, "Sebilj, Bascarsija, Sarajevo", rasvjeta, 6L, sNovo);
        prijavaRepo.save(p2);
        historijaRepo.save(new HistorijaPrijave(tp1, p2, 6L));

        // Prijava 3 - curenje vode (Rijeseno, arhivirana)
        Prijava p3 = new Prijava("Curenje vode - Ilindenska",
                "Curi voda iz vodovodne cijevi na Ilindenskoj ulici.",
                43.8512, 18.3891, "Ilindenska ulica 12, Sarajevo", vodovod, 5L, sRijeseno);
        p3.setPrioritet(PrioritetPrijave.HITNO);
        p3.setArhiviran(true);
        p3.setDatumZavrsetka(java.time.LocalDateTime.now().minusDays(2));
        prijavaRepo.save(p3);
        historijaRepo.save(new HistorijaPrijave(tp1, p3, 5L));
        historijaRepo.save(new HistorijaPrijave(tp2, p3, 1L));
        historijaRepo.save(new HistorijaPrijave(tp3, p3, 2L));
        historijaRepo.save(new HistorijaPrijave(tp4, p3, 3L));
        komentarRepo.save(new Komentar(3L, p3, "Rijeseno", "Cijev zamijenjena.", false));
        arhivaRepo.save(new Arhiva(p3, 5L));

        System.out.println(">>> Kreirane 3 demo prijave.");
        System.out.println(">>> Report Service: inicijalizacija zavrsena!");
    }
}
