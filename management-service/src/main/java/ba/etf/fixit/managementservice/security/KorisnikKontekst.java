package ba.etf.fixit.managementservice.security;

/**
 * Drzi informacije o trenutno autentificiranom korisniku
 * za trajanje jednog HTTP zahtjeva (ThreadLocal).
 *
 * Vrijednosti se postavljaju u GatewaySecurityFilter na pocetku
 * zahtjeva i brisu na kraju (da nema curenja izmedju zahtjeva).
 */
public class KorisnikKontekst {

    private static final ThreadLocal<KorisnikPodaci> trenutniKorisnik = new ThreadLocal<>();

    public static void postavi(KorisnikPodaci podaci) {
        trenutniKorisnik.set(podaci);
    }

    public static KorisnikPodaci dohvati() {
        return trenutniKorisnik.get();
    }

    public static void obrisi() {
        trenutniKorisnik.remove();
    }

    /**
     * Vraca email trenutnog korisnika ili null ako nije autentificiran.
     */
    public static String email() {
        KorisnikPodaci p = dohvati();
        return p != null ? p.email() : null;
    }

    /**
     * Vraca ulogu trenutnog korisnika ili prazan string.
     */
    public static String uloga() {
        KorisnikPodaci p = dohvati();
        return p != null ? p.uloga() : "";
    }

    public static Long korisnikId() {
        KorisnikPodaci p = dohvati();
        return p != null ? p.korisnikId() : null;
    }

    /**
     * Pomocni record za cuvanje podataka o korisniku.
     */
    public record KorisnikPodaci(Long korisnikId, String email, String uloga) {}
}
