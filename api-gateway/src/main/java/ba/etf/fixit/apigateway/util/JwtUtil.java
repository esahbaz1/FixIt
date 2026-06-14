package ba.etf.fixit.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * Pomocna klasa za rad sa JWT tokenima na nivou gatewaya.
 *
 * Gateway SAMO VERIFICIRA access tokene - nikad ih ne kreira.
 * Kreiranje tokena vrsi se iskljucivo u user-service pomocu privatnog kljuca.
 *
 * Gateway ima samo JAVNI kljuc (RS256) - kompromitovanje gatewaya ne daje
 * mogucnost kreiranja laznih tokena.
 */
@Component
public class JwtUtil {

    private final PublicKey publicKey;

    public JwtUtil(@Value("${jwt.public-key}") String publicKeyBase64) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            byte[] keyBytes = Base64.getDecoder().decode(
                    publicKeyBase64.replaceAll("-----.*-----", "").replaceAll("\\s", ""));
            this.publicKey = kf.generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new IllegalStateException("Ne mogu ucitati RSA javni kljuc za JWT verifikaciju: " + e.getMessage(), e);
        }
    }

    public Claims parsirajToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
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