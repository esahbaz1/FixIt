import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import T from "../styles/tokens";
import Icon from "./Icon";
import { useAuth } from "../context/AuthContext";
import { useNotifications } from "../context/useNotifications";

export default function AppShell({ children }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const { notifCount, resetCount } = useNotifications();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleTab = (path) => {
    if (path === "/notifikacije") resetCount();
    setMobileMenuOpen(false);
    navigate(path);
  };

  const isAdmin = user?.uloga === "ADMIN";
  const isRukovodilac = user?.uloga === "RUKOVODILAC";
  const isAdminOrRuk = isAdmin || isRukovodilac;

  const nav = [
    { path: "/",             label: "Pregled",        Icon: Icon.Grid  },
    { path: "/prijave",      label: "Prijave",         Icon: Icon.List  },
    { path: "/nova",         label: "Nova prijava",    Icon: Icon.Plus  },
    { path: "/notifikacije", label: "Obavijesti",      Icon: Icon.Bell, badge: notifCount },
    ...(isAdminOrRuk
      ? [
          { path: "/admin",           label: "Korisnici",    Icon: Icon.Users  },
          { path: "/admin-dashboard", label: "Dashboard",    Icon: Icon.Grid   },
          { path: "/statistika",      label: "Statistika",   Icon: Icon.Chart  },
        ]
      : []),
    { path: "/profil",       label: "Profil",          Icon: Icon.User  },
  ];

  function isActive(path) {
    if (path === "/") return location.pathname === "/";
    return location.pathname.startsWith(path);
  }

  return (
    <div style={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
      <header className="app-header">

        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <div style={{ width: 26, height: 26, borderRadius: 6, background: "#2ECC71", display: "flex", alignItems: "center", justifyContent: "center" }}>
            <svg width="12" height="12" viewBox="0 0 14 14" fill="none">
              <path d="M7 1L3 5.5h3v3L11 4H8V1z" fill="white" />
              <circle cx="7" cy="11" r="1.5" fill="white" opacity="0.6" />
            </svg>
          </div>
          <span style={{ fontSize: 14, fontWeight: 600, color: T.text, letterSpacing: "-0.01em" }}>FixIt</span>
        </div>

        <div className="divider" />

        <nav className="desktop-nav" style={{ display: "flex", gap: 2 }}>
          {nav.map(({ path, label, Icon: Ic, badge }) => (
            <button key={path} onClick={() => handleTab(path)} className={`nav-btn ${isActive(path) ? "active" : ""}`} style={{ position: "relative" }}>
              <Ic /> {label}
              {badge > 0 && (
                <span style={{ position: "absolute", top: 2, right: 2, minWidth: 16, height: 16, borderRadius: 8, background: "#E74C3C", color: "#fff", fontSize: 10, fontWeight: 700, display: "flex", alignItems: "center", justifyContent: "center", padding: "0 4px", lineHeight: 1 }}>
                  {badge > 99 ? "99+" : badge}
                </span>
              )}
            </button>
          ))}
        </nav>

        <div style={{ marginLeft: "auto", display: "flex", alignItems: "center", gap: 12 }}>

          <div className="user-pill" style={{ display: "flex", alignItems: "center", gap: 8, padding: "4px 10px 4px 6px", background: T.bgRaised, border: `1px solid ${T.line}`, borderRadius: 100 }}>
            <div style={{ width: 22, height: 22, borderRadius: "50%", background: "rgba(46,204,113,0.2)", border: "1px solid rgba(46,204,113,0.4)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 10, fontWeight: 600, color: "#2ECC71" }}>
              {(user?.ime?.[0] || user?.email?.[0] || "U").toUpperCase()}
            </div>
            <span className="user-name" style={{ fontSize: 12, fontWeight: 500, color: T.text }}>
              {user?.ime || user?.email?.split("@")[0]}
            </span>
            {user?.uloga && user.uloga !== "GRADJANIN" && (
              <span className="user-role" style={{ fontSize: 9, fontWeight: 600, letterSpacing: "0.06em", textTransform: "uppercase", color: "#F39C12", padding: "1px 5px", borderRadius: 3, background: "rgba(243,156,18,0.15)", border: "1px solid rgba(243,156,18,0.3)" }}>
                {user.uloga}
              </span>
            )}
          </div>

          <button onClick={logout} className="btn-ghost logout-btn" style={{ padding: "5px 10px", fontSize: 12 }}>
            <Icon.LogOut /> <span className="logout-text">Odjava</span>
          </button>

          <button
            className="hamburger-btn"
            onClick={() => setMobileMenuOpen(v => !v)}
            aria-label="Meni"
            style={{ display: "none", alignItems: "center", justifyContent: "center", width: 36, height: 36, borderRadius: 8, border: `1px solid ${T.line}`, background: mobileMenuOpen ? T.bgRaised : "transparent", color: T.text, cursor: "pointer" }}
          >
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              {mobileMenuOpen ? (
                <path d="M3 3l10 10M13 3L3 13" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
              ) : (
                <path d="M2 4h12M2 8h12M2 12h12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
              )}
            </svg>
          </button>
        </div>
      </header>

      {mobileMenuOpen && (
        <div className="mobile-nav-overlay" onClick={() => setMobileMenuOpen(false)} style={{ position: "fixed", top: 52, left: 0, right: 0, bottom: 0, zIndex: 99 }}>
          <nav
            onClick={e => e.stopPropagation()}
            style={{ background: "rgba(15,42,26,0.98)", backdropFilter: "blur(12px)", borderBottom: `1px solid ${T.line}`, padding: "8px 16px 16px" }}
          >
            {nav.map(({ path, label, Icon: Ic, badge }) => (
              <button key={path} onClick={() => handleTab(path)} className={`nav-btn mobile-nav-item ${isActive(path) ? "active" : ""}`} style={{ width: "100%", justifyContent: "flex-start", position: "relative", marginBottom: 2 }}>
                <Ic /> {label}
                {badge > 0 && (
                  <span style={{ marginLeft: "auto", minWidth: 20, height: 20, borderRadius: 10, background: "#E74C3C", color: "#fff", fontSize: 11, fontWeight: 700, display: "flex", alignItems: "center", justifyContent: "center", padding: "0 5px" }}>
                    {badge > 99 ? "99+" : badge}
                  </span>
                )}
              </button>
            ))}
          </nav>
        </div>
      )}

      <main className="app-main">
        {children}
      </main>
    </div>
  );
}
