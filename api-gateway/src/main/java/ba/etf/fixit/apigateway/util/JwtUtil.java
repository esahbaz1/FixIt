package ba.etf.fixit.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Pomocna klasa za rad sa JWT tokenima na nivou gatewaya.
 * Gateway samo VERIFICIRA access tokene — ne kreira ih.
 * Kreiranje tokena vrsi se iskljucivo u user-service.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private Key getKey() {
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT tajni kljuc mora imati najmanje 32 karaktera");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims parsirajToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Provjerava je li token valjan ACCESS token (ne refresh).
     * Refresh tokeni se validiraju iskljucivo u user-service.
     */
    public boolean jeValjanAccessToken(String token) {
        try {
            Claims claims = parsirajToken(token);
            boolean nijeIstekao = !claims.getExpiration().before(new Date());
            String tip = claims.get("tip", String.class);
            boolean jeAccessTip = "access".equals(tip);
            return nijeIstekao && jeAccessTip;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String dohvatiEmail(String token) {
        return parsirajToken(token).getSubject();
    }

    public String dohvatiUlogu(String token) {
        return parsirajToken(token).get("uloga", String.class);
    }

    public Long dohvatiKorisnikId(String token) {
        return parsirajToken(token).get("korisnikId", Long.class);
    }
}