package ba.etf.fixit.userservice.dto;

import ba.etf.fixit.userservice.model.UlogaKorisnika;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sadrzi access token (1h) i refresh token (7 dana).
 *
 * Tok:
 *  1. Klijent salje token u Authorization: Bearer <token> za svaki zahtjev
 *  2. Kada token istekne (401), salje refreshToken na POST /api/auth/refresh
 *  3. Pri logout-u poziva POST /api/auth/odjava sa refreshToken-om
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private Long id;
    private String email;
    private String ime;
    private String prezime;
    private UlogaKorisnika uloga;
    private String poruka;
    private String token;
    private String refreshToken;
}