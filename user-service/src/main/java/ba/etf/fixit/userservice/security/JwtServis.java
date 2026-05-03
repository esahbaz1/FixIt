package ba.etf.fixit.userservice.security;

import ba.etf.fixit.userservice.model.Korisnik;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kreira i validira JWT tokene.
 *
 * Access token  : 1h, nosi korisnicke podatke, salje se uz svaki zahtjev
 * Refresh token : 7 dana, koristi se SAMO za dobivanje novog access tokena
 *
 * Logout: refresh token se stavlja na in-memory blacklistu.
 * Access token ostaje validan do isteka (max 1h) — svjesna odluka jer
 * invalidacija access tokena zahtijeva Redis sto dodaje infrastrukturnu
 * zavisnost koja nije opravdana za aplikaciju ovog tipa.
 */
@Component
public class JwtServis {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiracija-ms}")
    private long expirationMs;

    @Value("${jwt.refresh-expiracija-ms}")
    private long refreshExpirationMs;

    private final Set<String> refreshTokenBlacklist =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
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
                .signWith(getKey(), SignatureAlgorithm.HS256)
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
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parsirajToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
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