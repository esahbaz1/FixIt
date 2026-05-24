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
        Statusi sNovo       = statusiRepo.save(new Statusi(null, "Novo",       "Novoprijavljen problem"));
        Statusi sDodijeljeno= statusiRepo.save(new Statusi(null, "Dodijeljeno","Dodijeljen nadleznoj sluzbi"));
        Statusi sURadu      = statusiRepo.save(new Statusi(null, "U radu",     "Radnici rade na rjesavanju"));
        Statusi sRijeseno   = statusiRepo.save(new Statusi(null, "Rijeseno",   "Problem uspjesno rijesen"));
        Statusi sZatvoreno  = statusiRepo.save(new Statusi(null, "Zatvoreno",  "Prijava zatvorena"));
        System.out.println(">>> Kreirano 5 statusa.");

        // Tipovi promjena
        TipPromjene tp1 = tipRepo.save(new TipPromjene(null, null,        "Novo"));
        TipPromjene tp2 = tipRepo.save(new TipPromjene(null, "Novo",      "Dodijeljeno"));
        TipPromjene tp3 = tipRepo.save(new TipPromjene(null, "Dodijeljeno","U radu"));
        TipPromjene tp4 = tipRepo.save(new TipPromjene(null, "U radu",    "Rijeseno"));
        TipPromjene tp5 = tipRepo.save(new TipPromjene(null, "Rijeseno",  "Zatvoreno"));
        TipPromjene tp6 = tipRepo.save(new TipPromjene(null, "Zatvoreno", "Novo"));
        System.out.println(">>> Kreirano 6 tipova promjena.");

        // Kategorije
        Kategorija putCesta  = kategorijaRepo.save(new Kategorija(null, "Put/cesta",     "Ostecenja kolovoza",    1L));
        Kategorija rasvjeta  = kategorijaRepo.save(new Kategorija(null, "Javna rasvjeta","Pokvarena rasvjeta",    2L));
        Kategorija vodovod   = kategorijaRepo.save(new Kategorija(null, "Vodovod",       "Kvarovi vodovodne mreze",1L));
        Kategorija zelenilo  = kategorijaRepo.save(new Kategorija(null, "Zelenilo",      "Odrzavanje parkova",    4L));
        Kategorija otpad     = kategorijaRepo.save(new Kategorija(null, "Otpad",         "Nelegalne deponije",    1L));
        Kategorija saobracaj = kategorijaRepo.save(new Kategorija(null, "Saobracaj",     "Semafori i znakovi",    3L));
        Kategorija ostalo    = kategorijaRepo.save(new Kategorija(null, "Ostalo",        "Ostali problemi",       1L));
        System.out.println(">>> Kreirano 7 kategorija.");

        // Prijava 1 - rupa na putu (U radu)
        Prijava p1 = new Prijava();
        p1.setNaslov("Velika rupa na asfaltu - Titova ulica");
        p1.setOpis("Na raskrsnici Titove i Marsala Tita postoji rupa oko 30cm dubine.");
        p1.setLatitude(43.8563);
        p1.setLongitude(18.4131);
        p1.setAdresa("Titova ulica bb, Sarajevo");
        p1.setKategorija(putCesta);
        p1.setKorisnikId(5L);
        p1.setStatus(sURadu);
        p1.setPrioritet(PrioritetPrijave.VISOK);
        p1.setGrdSluzbald(1L);
        prijavaRepo.save(p1);
        fotografijaRepo.save(new Fotografija(null, p1, "/uploads/prijave/1/rupa-titova.jpg", null));
        komentarRepo.save(new Komentar(null, 5L, p1, "Hitna intervencija", "Molim hitnu intervenciju!", false, null));
        komentarRepo.save(new Komentar(null, 2L, p1, "Interna napomena", "Dodijeljen radniku.", true, null));
        historijaRepo.save(new HistorijaPrijave(null, tp1, p1, 5L, null));
        historijaRepo.save(new HistorijaPrijave(null, tp2, p1, 1L, null));
        historijaRepo.save(new HistorijaPrijave(null, tp3, p1, 2L, null));
        validacijaRepo.save(new Validacija(null, p1, 6L, true, null));

        // Prijava 2 - rasvjeta (Novo)
        Prijava p2 = new Prijava();
        p2.setNaslov("Pokvarena ulicna svjetla - Bascarsija");
        p2.setOpis("Na Bascarsiji ne rade 3 ulicna svjetla vec sedmicu.");
        p2.setLatitude(43.8601);
        p2.setLongitude(18.4311);
        p2.setAdresa("Sebilj, Bascarsija, Sarajevo");
        p2.setKategorija(rasvjeta);
        p2.setKorisnikId(6L);
        p2.setStatus(sNovo);
        prijavaRepo.save(p2);
        historijaRepo.save(new HistorijaPrijave(null, tp1, p2, 6L, null));

        // Prijava 3 - curenje vode (Rijeseno, arhivirana)
        // FIX: datumZavrsetka mora biti NAKON datumPodnosenja (@PrePersist postavlja datumPodnosenja na now())
        // Koristimo minusDays(5) za podnosenje i minusDays(2) za zavrsetak = 72h razlika
        Prijava p3 = new Prijava();
        p3.setNaslov("Curenje vode - Ilindenska");
        p3.setOpis("Curi voda iz vodovodne cijevi na Ilindenskoj ulici.");
        p3.setLatitude(43.8512);
        p3.setLongitude(18.3891);
        p3.setAdresa("Ilindenska ulica 12, Sarajevo");
        p3.setKategorija(vodovod);
        p3.setKorisnikId(5L);
        p3.setStatus(sRijeseno);
        p3.setPrioritet(PrioritetPrijave.HITNO);
        p3.setArhiviran(true);
        prijavaRepo.save(p3);
        // Nakon save(), @PrePersist je postavio datumPodnosenja = now()
        // Sada ručno postavljamo datumZavrsetka = now() + 3 dana da bude pozitivna razlika
        // Ali ne možemo promijeniti datumPodnosenja jer je updatable=false
        // Zaobilazno rješenje: koristimo native SQL update ili setujemo zavrsetak u budućnosti
        // Najjednostavnije: koristimo JPQL update
        prijavaRepo.flush();
        // Postavljamo datumZavrsetka 72 sata NAKON datumPodnosenja (koji je upravo kreiran = now())
        p3.setDatumZavrsetka(java.time.LocalDateTime.now().plusHours(72));
        prijavaRepo.save(p3);

        historijaRepo.save(new HistorijaPrijave(null, tp1, p3, 5L, null));
        historijaRepo.save(new HistorijaPrijave(null, tp2, p3, 1L, null));
        historijaRepo.save(new HistorijaPrijave(null, tp3, p3, 2L, null));
        historijaRepo.save(new HistorijaPrijave(null, tp4, p3, 3L, null));
        komentarRepo.save(new Komentar(null, 3L, p3, "Rijeseno", "Cijev zamijenjena.", false, null));
        arhivaRepo.save(new Arhiva(null, p3, 5L));

        System.out.println(">>> Kreirane 3 demo prijave.");
        System.out.println(">>> Report Service: inicijalizacija zavrsena!");
    }
}
