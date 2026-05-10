import T from "../styles/tokens";
import Icon from "./Icon";
import { useAuth } from "../context/AuthContext";

export default function AppShell({ children, activeTab, setActiveTab }) {
  const { user, logout } = useAuth();
  const nav = [
    { id: "dashboard", label: "Pregled",      Icon: Icon.Grid },
    { id: "prijave",   label: "Prijave",      Icon: Icon.List },
    { id: "nova",      label: "Nova prijava", Icon: Icon.Plus },
    { id: "profil",    label: "Profil",       Icon: Icon.User },
  ];

  return (
    <div style={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
      <header style={{
        height: 52,
        background: "rgba(15,61,40,0.97)",
        backdropFilter: "blur(12px)",
        borderBottom: `1px solid ${T.line}`,
        display: "flex", alignItems: "center",
        padding: "0 24px", gap: 24,
        position: "sticky", top: 0, zIndex: 100,
        flexShrink: 0,
      }}>
        {/* Logo */}
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <div style={{
            width: 26, height: 26, borderRadius: 6,
            background: T.blue,
            display: "flex", alignItems: "center", justifyContent: "center",
          }}>
            <svg width="12" height="12" viewBox="0 0 14 14" fill="none">
              <path d="M7 1L3 5.5h3v3L11 4H8V1z" fill="white"/>
              <circle cx="7" cy="11" r="1.5" fill="white" opacity="0.6"/>
            </svg>
          </div>
          <span style={{ fontSize: 14, fontWeight: 600, color: T.text, letterSpacing: "-0.01em" }}>FixIt</span>
        </div>

        <div className="divider"/>

        {/* Nav */}
        <nav style={{ display: "flex", gap: 2 }}>
          {nav.map(({ id, label, Icon: Ic }) => (
            <button key={id} onClick={() => setActiveTab(id)} className={`nav-btn ${activeTab === id ? "active" : ""}`}>
              <Ic/> {label}
            </button>
          ))}
        </nav>

        <div style={{ marginLeft: "auto", display: "flex", alignItems: "center", gap: 12 }}>
          {/* User pill */}
          <div style={{
            display: "flex", alignItems: "center", gap: 8,
            padding: "4px 10px 4px 6px",
            background: T.bgRaised, border: `1px solid ${T.line}`,
            borderRadius: 100,
          }}>
            <div style={{
              width: 22, height: 22, borderRadius: "50%",
              background: T.blueDim, border: `1px solid ${T.blueBorder}`,
              display: "flex", alignItems: "center", justifyContent: "center",
              fontSize: 10, fontWeight: 600, color: T.blue,
            }}>
              {(user?.ime?.[0] || user?.email?.[0] || "U").toUpperCase()}
            </div>
            <span style={{ fontSize: 12, fontWeight: 500, color: T.text }}>
              {user?.ime || user?.email?.split("@")[0]}
            </span>
          </div>

          <button onClick={logout} className="btn-ghost" style={{ padding: "5px 10px", fontSize: 12 }}>
            <Icon.LogOut/> Odjava
          </button>
        </div>
      </header>

      <main style={{ flex: 1, padding: "40px 48px", width: "100%" }}>
        {children}
      </main>
    </div>
  );
}
