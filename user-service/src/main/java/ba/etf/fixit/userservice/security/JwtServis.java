package ba.etf.fixit.userservice.security;

import ba.etf.fixit.userservice.model.InvalidisanToken;
import ba.etf.fixit.userservice.model.Korisnik;
import ba.etf.fixit.userservice.repository.InvalidisanTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;

@Component
public class JwtServis {

    private static final Logger log = LoggerFactory.getLogger(JwtServis.class);

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    @Value("${jwt.expiracija-ms}")
    private long expirationMs;

    @Value("${jwt.refresh-expiracija-ms}")
    private long refreshExpirationMs;

    private final InvalidisanTokenRepository blacklistaRepo;

    @Autowired
    public JwtServis(
            @Value("${jwt.private-key}") String privateKeyBase64,
            @Value("${jwt.public-key}") String publicKeyBase64,
            InvalidisanTokenRepository blacklistaRepo) {
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
        this.blacklistaRepo = blacklistaRepo;
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

    @Transactional(readOnly = true)
    public boolean jeValidanAccessToken(String token) {
        try {
            Claims claims = parsirajToken(token);
            if (claims.getExpiration().before(new Date())) return false;
            if (!"access".equals(claims.get("tip", String.class))) return false;
            // Provjeri blacklistu (pokriva slucaj kada je access token invalidisan pri logostu)
            return !blacklistaRepo.existsByTokenHash(hashToken(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public boolean jeValidanRefreshToken(String token) {
        try {
            Claims claims = parsirajToken(token);
            if (claims.getExpiration().before(new Date())) return false;
            if (!"refresh".equals(claims.get("tip", String.class))) return false;
            return !blacklistaRepo.existsByTokenHash(hashToken(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Transactional
    public void invalidisiRefreshToken(String refreshToken) {
        try {
            Claims claims = parsirajToken(refreshToken);
            String hash = hashToken(refreshToken);
            if (!blacklistaRepo.existsByTokenHash(hash)) {
                LocalDateTime datumIsteka = claims.getExpiration()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                blacklistaRepo.save(new InvalidisanToken(hash, datumIsteka, claims.getSubject()));
                log.info("Refresh token invalidisan za korisnika: {}", claims.getSubject());
            }
        } catch (Exception e) {
            log.warn("Nije moguce invalidisati refresh token: {}", e.getMessage());
        }
    }


    @Transactional
    public void invalidisiAccessToken(String accessToken) {
        try {
            Claims claims = parsirajToken(accessToken);
            if (!"access".equals(claims.get("tip", String.class))) return;
            String hash = hashToken(accessToken);
            if (!blacklistaRepo.existsByTokenHash(hash)) {
                LocalDateTime datumIsteka = claims.getExpiration()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                blacklistaRepo.save(new InvalidisanToken(hash, datumIsteka, claims.getSubject()));
                log.info("Access token invalidisan za korisnika: {}", claims.getSubject());
            }
        } catch (Exception e) {
            log.warn("Nije moguce invalidisati access token: {}", e.getMessage());
        }
    }

    /** Automatsko ciscenje isteklih zapisa - svaki dan u ponoc. */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void ocistiIstekleTokene() {
        int obrisano = blacklistaRepo.obrisiIstekle(LocalDateTime.now());
        if (obrisano > 0) {
            log.info("Obrisano {} isteklih zapisa iz token blackliste.", obrisano);
        }
    }

    public String dohvatiEmail(String token) { return parsirajToken(token).getSubject(); }
    public Long dohvatiKorisnikId(String token) { return parsirajToken(token).get("korisnikId", Long.class); }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Ne mogu hashirati token", e);
        }
    }
}
