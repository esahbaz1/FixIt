import T from "../styles/tokens";
import { useAuth } from "../context/AuthContext";
import PageHeader from "../components/PageHeader";

export default function ProfilPage() {
  const { user } = useAuth();
  const initial = (user?.ime?.[0] || user?.email?.[0] || "U").toUpperCase();
  const fullName = user?.ime ? `${user.ime} ${user.prezime || ""}`.trim() : user?.email?.split("@")[0];

  return (
    <div style={{ animation: "fadeIn 0.3s ease" }}>
      <PageHeader title="Profil" sub="Informacije o nalogu"/>

      <div style={{ display: "grid", gridTemplateColumns: "280px 1fr", gap: 16 }}>
        {/* Avatar card */}
        <div className="card" style={{ padding: 28, textAlign: "center" }}>
          <div style={{
            width: 72, height: 72, borderRadius: "50%",
            background: T.blueDim, border: `1px solid ${T.blueBorder}`,
            display: "flex", alignItems: "center", justifyContent: "center",
            fontSize: 26, fontWeight: 600, color: T.blue,
            margin: "0 auto 16px",
          }}>
            {initial}
          </div>

          <div style={{ fontSize: 15, fontWeight: 600, color: T.text, marginBottom: 4, letterSpacing: "-0.01em" }}>{fullName}</div>
          <div style={{ fontSize: 12, color: T.textMuted, marginBottom: 16 }}>{user?.email}</div>

          <span style={{
            display: "inline-block",
            background: T.blueDim, border: `1px solid ${T.blueBorder}`,
            color: T.blue, padding: "3px 10px", borderRadius: 4,
            fontSize: 10, fontWeight: 600, letterSpacing: "0.08em", textTransform: "uppercase",
          }}>
            {user?.uloga || "GRADJANIN"}
          </span>

          <div style={{ marginTop: 24, paddingTop: 20, borderTop: `1px solid ${T.line}`, textAlign: "left" }}>
            <div style={{ fontSize: 10, fontWeight: 500, color: T.textMuted, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: 6 }}>ID</div>
            <code style={{ fontSize: 16, fontWeight: 600, color: T.text, fontFamily: "'Geist Mono', monospace" }}>
              #{user?.id || "—"}
            </code>
          </div>
        </div>

        {/* Info card */}
        <div className="card" style={{ padding: 28 }}>
          <div style={{ fontSize: 13, fontWeight: 500, color: T.text, marginBottom: 20 }}>Informacije o nalogu</div>

          {[
            { label: "Ime i prezime", value: fullName || "—" },
            { label: "Email adresa",  value: user?.email || "—" },
            { label: "Uloga",         value: user?.uloga || "GRADJANIN" },
            { label: "Status naloga", value: "Aktivan", color: T.green },
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

          <div style={{
            marginTop: 16, padding: "16px 18px",
            background: T.blueDim, border: `1px solid ${T.blueBorder}`,
            borderRadius: 8,
          }}>
            <div style={{ fontSize: 12, fontWeight: 500, color: T.blue, marginBottom: 6 }}>O platformi</div>
            <p style={{ fontSize: 12, color: T.textSub, lineHeight: 1.7, margin: 0 }}>
              Svaka prijavljena prijava doprinosi boljoj infrastrukturi. Hvala što koristite FixIt.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
