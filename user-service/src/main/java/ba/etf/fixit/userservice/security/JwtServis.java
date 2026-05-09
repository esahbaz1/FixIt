package ba.etf.fixit.userservice.security;

import ba.etf.fixit.userservice.model.Korisnik;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kreira i validira JWT tokene pomocu RSA asimetricne kriptografije.
 *
 * SIGURNOST:
 *  - user-service ima PRIVATNI kljuc i JEDINI moze potpisivati tokene.
 *  - api-gateway ima samo JAVNI kljuc i moze SAMO verificirati potpis.
 *  - Nijedan drugi mikroservis ne treba nikakav JWT kljuc.
 *
 * Access token  : 1h, nosi korisnicke podatke, salje se uz svaki zahtjev
 * Refresh token : 7 dana, koristi se SAMO za dobivanje novog access tokena
 *
 * Logout: refresh token se stavlja na in-memory blacklistu.
 */
@Component
public class JwtServis {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    @Value("${jwt.expiracija-ms}")
    private long expirationMs;

    @Value("${jwt.refresh-expiracija-ms}")
    private long refreshExpirationMs;

    private final Set<String> refreshTokenBlacklist =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    public JwtServis(
            @Value("${jwt.private-key}") String privateKeyBase64,
            @Value("${jwt.public-key}") String publicKeyBase64) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");

            byte[] privateBytes = Base64.getDecoder().decode(
                    privateKeyBase64.replaceAll("-----.*-----", "").replaceAll("\\s", ""));
            this.privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));

            byte[] publicBytes = Base64.getDecoder().decode(
                    publicKeyBase64.replaceAll("-----.*-----", "").replaceAll("\\s", ""));
            this.publicKey = kf.generatePublic(new X509EncodedKeySpec(publicBytes));

        } catch (Exception e) {
            throw new IllegalStateException("Ne mogu ucitati RSA kljuceve za JWT: " + e.getMessage(), e);
        }
    }

    public String kreirajToken(Korisnik korisnik) {
        Date sada = new Date();
        return Jwts.builder()
                .setSubject(korisnik.getEmail())
                .claim("korisnikId", korisnik.getId())
                .claim("uloga", korisnik.getUloga().name())
                .claim("ime", korisnik.getIme())
                .claim("prezime", korisnik.getPrezime())
                .claim("tip", "access")
                .setIssuedAt(sada)
                .setExpiration(new Date(sada.getTime() + expirationMs))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String kreirajRefreshToken(Korisnik korisnik) {
        Date sada = new Date();
        return Jwts.builder()
                .setSubject(korisnik.getEmail())
                .claim("korisnikId", korisnik.getId())
                .claim("tip", "refresh")
                .setIssuedAt(sada)
                .setExpiration(new Date(sada.getTime() + refreshExpirationMs))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public Claims parsirajToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean jeValidanAccessToken(String token) {
        try {
            Claims claims = parsirajToken(token);
            return !claims.getExpiration().before(new Date())
                    && "access".equals(claims.get("tip", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean jeValidanRefreshToken(String token) {
        try {
            if (refreshTokenBlacklist.contains(token)) return false;
            Claims claims = parsirajToken(token);
            return !claims.getExpiration().before(new Date())
                    && "refresh".equals(claims.get("tip", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public void invalidisiRefreshToken(String refreshToken) {
        refreshTokenBlacklist.add(refreshToken);
    }

    public String dohvatiEmail(String token) {
        return parsirajToken(token).getSubject();
    }

    public Long dohvatiKorisnikId(String token) {
        return parsirajToken(token).get("korisnikId", Long.class);
    }
}