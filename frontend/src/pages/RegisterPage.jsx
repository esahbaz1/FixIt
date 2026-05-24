import { useState } from "react";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import AuthLayout from "../components/AuthLayout";
import Spinner from "../components/Spinner";

// UX-07: Helper za procjenu jačine lozinke
function passwordStrength(password) {
  if (!password) return null;
  let score = 0;
  if (password.length >= 8) score++;
  if (/[A-Z]/.test(password)) score++;
  if (/[0-9]/.test(password)) score++;
  if (/[^A-Za-z0-9]/.test(password)) score++;
  if (score <= 1) return { level: "slaba", color: T.red, width: "25%" };
  if (score === 2) return { level: "osrednja", color: T.amber, width: "50%" };
  if (score === 3) return { level: "dobra", color: "#F39C12", width: "75%" };
  return { level: "jaka", color: T.green, width: "100%" };
}

// UX-06: Regex koji provjerava prisustvo TLD-a
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

export default function RegisterPage({ onSuccess, switchToLogin }) {
  const [form, setForm] = useState({
    ime: "",
    prezime: "",
    email: "",
    lozinka: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});

  const strength = passwordStrength(form.lozinka);

  function validate() {
    const errors = {};
    // UX-06: Provjera email formata s TLD-om
    if (form.email && !EMAIL_REGEX.test(form.email)) {
      errors.email = "Unesite valjanu email adresu (npr. korisnik@domena.ba)";
    }
    // UX-07: Provjera kompleksnosti lozinke
    if (form.lozinka) {
      if (form.lozinka.length < 8) {
        errors.lozinka = "Lozinka mora imati najmanje 8 karaktera.";
      } else if (!/[A-Z]/.test(form.lozinka)) {
        errors.lozinka = "Lozinka mora sadržavati barem jedno veliko slovo.";
      } else if (!/[0-9]/.test(form.lozinka)) {
        errors.lozinka = "Lozinka mora sadržavati barem jedan broj.";
      } else if (!/[^A-Za-z0-9]/.test(form.lozinka)) {
        errors.lozinka = "Lozinka mora sadržavati barem jedan specijalni karakter.";
      }
    }
    return errors;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const errors = validate();
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }
    setFieldErrors({});
    setLoading(true);
    setError("");
    try {
      await apiCall("/api/auth/registracija", {
        method: "POST",
        body: JSON.stringify(form),
      });
      onSuccess();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout>
      <div style={{ marginBottom: 36 }}>
        <h2
          style={{
            fontSize: 24,
            fontWeight: 600,
            letterSpacing: "-0.02em",
            color: T.text,
            marginBottom: 6,
          }}
        >
          Kreirajte nalog
        </h2>
        <p style={{ color: T.textSub, fontSize: 13 }}>
          Brza registracija bez komplikacija
        </p>
      </div>

      {error && (
        <div
          style={{
            background: T.redDim,
            border: `1px solid ${T.redBorder}`,
            color: T.red,
            padding: "10px 14px",
            borderRadius: 8,
            marginBottom: 20,
            fontSize: 13,
          }}
        >
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "1fr 1fr",
            gap: 10,
            marginBottom: 14,
          }}
        >
          <div>
            <label className="label">Ime</label>
            <input
              type="text"
              required
              placeholder="Adnan"
              value={form.ime}
              onChange={(e) => setForm({ ...form, ime: e.target.value })}
              className="input-field"
            />
          </div>
          <div>
            <label className="label">Prezime</label>
            <input
              type="text"
              required
              placeholder="Hodžić"
              value={form.prezime}
              onChange={(e) => setForm({ ...form, prezime: e.target.value })}
              className="input-field"
            />
          </div>
        </div>

        {/* UX-06: Email s custom validacijom */}
        <div style={{ marginBottom: 14 }}>
          <label className="label">Email</label>
          <input
            type="email"
            required
            placeholder="adnan@email.com"
            value={form.email}
            onChange={(e) => {
              setForm({ ...form, email: e.target.value });
              if (fieldErrors.email) setFieldErrors({ ...fieldErrors, email: "" });
            }}
            className="input-field"
            style={
              fieldErrors.email
                ? { borderColor: T.red }
                : {}
            }
          />
          {fieldErrors.email && (
            <p style={{ fontSize: 11, color: T.red, marginTop: 4 }}>
              {fieldErrors.email}
            </p>
          )}
        </div>

        {/* UX-07: Lozinka s indikatorom jačine */}
        <div style={{ marginBottom: 24 }}>
          <label className="label">Lozinka</label>
          <input
            type="password"
            required
            placeholder="Min. 8 karaktera"
            value={form.lozinka}
            onChange={(e) => {
              setForm({ ...form, lozinka: e.target.value });
              if (fieldErrors.lozinka) setFieldErrors({ ...fieldErrors, lozinka: "" });
            }}
            className="input-field"
            style={fieldErrors.lozinka ? { borderColor: T.red } : {}}
          />

          {/* Indikator jačine lozinke */}
          {form.lozinka && strength && (
            <div style={{ marginTop: 8 }}>
              <div
                style={{
                  height: 3,
                  background: T.bgActive,
                  borderRadius: 2,
                  overflow: "hidden",
                  marginBottom: 4,
                }}
              >
                <div
                  style={{
                    height: "100%",
                    width: strength.width,
                    background: strength.color,
                    borderRadius: 2,
                    transition: "width 0.3s ease, background 0.3s ease",
                  }}
                />
              </div>
              <span style={{ fontSize: 11, color: strength.color }}>
                Jačina lozinke: {strength.level}
              </span>
            </div>
          )}

          {/* Zahtjevi lozinke */}
          <div style={{ marginTop: 8, display: "flex", flexDirection: "column", gap: 3 }}>
            {[
              { check: form.lozinka.length >= 8,           text: "Najmanje 8 karaktera" },
              { check: /[A-Z]/.test(form.lozinka),         text: "Jedno veliko slovo" },
              { check: /[0-9]/.test(form.lozinka),         text: "Jedan broj" },
              { check: /[^A-Za-z0-9]/.test(form.lozinka), text: "Jedan specijalni karakter (!@#$...)" },
            ].map(({ check, text }) => (
              <span
                key={text}
                style={{
                  fontSize: 11,
                  color: check ? T.green : T.textMuted,
                  display: "flex",
                  alignItems: "center",
                  gap: 5,
                }}
              >
                {check ? "✓" : "○"} {text}
              </span>
            ))}
          </div>

          {fieldErrors.lozinka && (
            <p style={{ fontSize: 11, color: T.red, marginTop: 4 }}>
              {fieldErrors.lozinka}
            </p>
          )}
        </div>

        <button
          type="submit"
          disabled={loading}
          className="btn-prim"
          style={{ width: "100%", padding: "10px" }}
        >
          {loading ? (
            <>
              <Spinner size={16} color="#fff" /> Kreiranje...
            </>
          ) : (
            "Kreiraj nalog"
          )}
        </button>
      </form>

      <p
        style={{
          marginTop: 24,
          textAlign: "center",
          fontSize: 13,
          color: T.textSub,
        }}
      >
        Već imate nalog?{" "}
        <button
          onClick={switchToLogin}
          style={{
            background: "none",
            border: "none",
            color: T.blue,
            fontWeight: 500,
            fontSize: 13,
            cursor: "pointer",
            padding: 0,
          }}
        >
          Prijavite se
        </button>
      </p>
    </AuthLayout>
  );
}
