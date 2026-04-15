package ba.etf.fixit.userservice.dto;
import jakarta.validation.constraints.*;

public class LoginRequestDTO {
    @Email(message = "Email mora biti u ispravnom formatu")
    @NotBlank(message = "Email ne smije biti prazan")
    private String email;
    @NotBlank(message = "Lozinka ne smije biti prazna")
    private String lozinka;

    public String getEmail() { return email; } public void setEmail(String v) { this.email = v; }
    public String getLozinka() { return lozinka; } public void setLozinka(String v) { this.lozinka = v; }
}
