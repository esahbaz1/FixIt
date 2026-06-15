import { useState, useEffect } from "react";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import { useAuth } from "../context/AuthContext";
import PageHeader from "../components/PageHeader";
import Spinner from "../components/Spinner";
import Icon from "../components/Icon";

export default function ProfilPage() {
  const { user, showToast } = useAuth();
  const [profil, setProfil] = useState(null);
  const [loading, setLoading] = useState(true);

  // Dohvati svježe podatke o korisniku sa servera
  useEffect(() => {
    if (!user?.id) return;
    apiCall(`/api/korisnici/${user.id}`)
      .then(data => setProfil(data))
      .catch(() => setProfil(null))
      .finally(() => setLoading(false));
  }, [user?.id]);

  const ime = profil?.ime || user?.ime || "—";
  const prezime = profil?.prezime || user?.prezime || "—";
  const fullName = `${ime} ${prezime}`.trim();
  const initial = (ime?.[0] || user?.email?.[0] || "U").toUpperCase();

  return (
    <div style={{ animation: "fadeIn 0.3s ease" }}>
      <PageHeader title="Profil" sub="Informacije o vašem nalogu"/>

      <div style={{ display: "grid", gridTemplateColumns: "280px 1fr", gap: 16 }}>

        {/* ─── Avatar kartica ─────────────────────────────── */}
        <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
          <div className="card" style={{ padding: 28, textAlign: "center" }}>
            <div style={{
              width: 72, height: 72, borderRadius: "50%",
              background: "linear-gradient(135deg, rgba(46,204,113,0.3), rgba(27,107,69,0.5))",
              border: "2px solid rgba(46,204,113,0.4)",
              display: "flex", alignItems: "center", justifyContent: "center",
              fontSize: 26, fontWeight: 600, color: "#2ECC71",
              margin: "0 auto 16px",
            }}>
              {initial}
            </div>

            {loading ? (
              <div style={{ display: "flex", justifyContent: "center" }}><Spinner/></div>
            ) : (
              <>
                <div style={{ fontSize: 15, fontWeight: 600, color: T.text, marginBottom: 4, letterSpacing: "-0.01em" }}>{fullName}</div>
                <div style={{ fontSize: 12, color: T.textMuted, marginBottom: 16 }}>{profil?.email || user?.email}</div>
                <span style={{
                  display: "inline-block",
                  background: "rgba(46,204,113,0.15)", border: "1px solid rgba(46,204,113,0.3)",
                  color: "#2ECC71", padding: "3px 10px", borderRadius: 4,
                  fontSize: 10, fontWeight: 600, letterSpacing: "0.08em", textTransform: "uppercase",
                }}>
                  {profil?.uloga || user?.uloga || "GRADJANIN"}
                </span>
              </>
            )}

            {}
            <div style={{ marginTop: 24, paddingTop: 20, borderTop: `1px solid ${T.line}`, textAlign: "left" }}>
              <div style={{ fontSize: 10, fontWeight: 500, color: T.textMuted, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: 6 }}>ID Korisnika</div>
              <code style={{ fontSize: 13, fontWeight: 600, color: T.textSub, fontFamily: "'Geist Mono', monospace", letterSpacing: "0.05em" }}>
                {user?.id
                  ? String(user.id).length > 8
                    ? `${String(user.id).slice(0, 4)}...${String(user.id).slice(-4)}`
                    : `${"*".repeat(Math.max(0, String(user.id).length - 1))}${String(user.id).slice(-1)}`
                  : "—"}
              </code>
            </div>

            {profil?.datumKreiranja && (
              <div style={{ marginTop: 16, paddingTop: 16, borderTop: `1px solid ${T.line}`, textAlign: "left" }}>
                <div style={{ fontSize: 10, fontWeight: 500, color: T.textMuted, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: 6 }}>Nalog kreiran</div>
                <div style={{ fontSize: 12, color: T.textSub }}>
                  {new Date(profil.datumKreiranja).toLocaleDateString("bs", { day: "numeric", month: "long", year: "numeric" })}
                </div>
              </div>
            )}
          </div>


        </div>

        {/* ─── Desna kolona ───────────────────────────────── */}
        <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>

          {/* Informacije o nalogu — samo čitanje */}
          <div className="card" style={{ padding: 28 }}>
            <div style={{ fontSize: 13, fontWeight: 500, color: T.text, marginBottom: 20 }}>
              Informacije o nalogu
            </div>

            {loading ? (
              <div style={{ display: "flex", justifyContent: "center", padding: 32 }}><Spinner size={28}/></div>
            ) : (
              <div>
                {[
                  { label: "Ime",           value: ime },
                  { label: "Prezime",       value: prezime },
                  { label: "Email adresa",  value: profil?.email || user?.email || "—" },
                  { label: "Uloga",         value: profil?.uloga || user?.uloga || "GRADJANIN" },
                  { label: "Status naloga", value: profil?.aktivan !== false ? "Aktivan" : "Neaktivan",
                    color: profil?.aktivan !== false ? "#2ECC71" : T.red },
                ].map(item => (
                  <div key={item.label} style={{
                    display: "flex", alignItems: "center", justifyContent: "space-between",
                    padding: "13px 16px",
                    background: T.bgRaised, borderRadius: 8,
                    border: `1px solid ${T.line}`, marginBottom: 8,
                  }}>
                    <span style={{ fontSize: 12, color: T.textMuted }}>{item.label}</span>
                    <span style={{ fontSize: 13, fontWeight: 500, color: item.color || T.text }}>{item.value}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Napomena o ograničenjima */}
          <div className="card" style={{
            padding: 22,
            borderColor: T.amberBorder,
            background: T.amberDim,
          }}>
            <div style={{ display: "flex", gap: 12, alignItems: "flex-start" }}>
              <span style={{ color: T.amber, flexShrink: 0, marginTop: 1 }}><Icon.Info/></span>
              <div>
                <div style={{ fontSize: 13, fontWeight: 500, color: T.amber, marginBottom: 6 }}>
                  Izmjena podataka nije dostupna
                </div>
                <p style={{ fontSize: 12, color: T.textSub, lineHeight: 1.6, margin: 0 }}>
                  Podaci o nalogu su samo za čitanje. Obratite se administratoru za eventualne izmjene.
                </p>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
