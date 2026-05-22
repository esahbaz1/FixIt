import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import T from "../styles/tokens";
import Icon from "./Icon";
import { useAuth } from "../context/AuthContext";
import { apiCall } from "../api/client";

// UX-03: AppShell koristi React Router – nema više useState za aktivni tab.
// useLocation() daje trenutni URL pa možemo highlightati aktivan nav item.
// useNavigate() zamjenjuje setActiveTab prop.
export default function AppShell({ children }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const [notifCount, setNotifCount] = useState(0);

  // Periodično dohvatamo broj nepročitanih notifikacija
  useEffect(() => {
    if (!user?.id) return;
    const fetchNotif = () => {
      apiCall(`/api/notifikacije/korisnik/${user.id}/broj-neprocitanih`)
        .then((d) => setNotifCount(d?.brojNeprocitanih || 0))
        .catch(() => {});
    };
    fetchNotif();
    const interval = setInterval(fetchNotif, 30000);
    return () => clearInterval(interval);
  }, [user?.id]);

  const handleTab = (path) => {
    if (path === "/notifikacije") setNotifCount(0);
    navigate(path);
  };

  const nav = [
    { path: "/",              label: "Pregled",      Icon: Icon.Grid  },
    { path: "/prijave",       label: "Prijave",      Icon: Icon.List  },
    { path: "/nova",          label: "Nova prijava", Icon: Icon.Plus  },
    { path: "/notifikacije",  label: "Obavijesti",   Icon: Icon.Bell, badge: notifCount },
    ...((user?.uloga === "RUKOVODILAC" || user?.uloga === "ADMIN")
      ? [{ path: "/admin", label: "Korisnici", Icon: Icon.Users }]
      : []),
    { path: "/profil",        label: "Profil",       Icon: Icon.User  },
  ];

  // Aktivan tab: ruta "/" je aktivan samo ako smo tačno na "/",
  // ostali su aktivni ako pathname počinje s path-om
  function isActive(path) {
    if (path === "/") return location.pathname === "/";
    return location.pathname.startsWith(path);
  }

  return (
    <div style={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
      <header
        style={{
          height: 52,
          background: "rgba(15,61,40,0.97)",
          backdropFilter: "blur(12px)",
          borderBottom: `1px solid ${T.line}`,
          display: "flex",
          alignItems: "center",
          padding: "0 24px",
          gap: 24,
          position: "sticky",
          top: 0,
          zIndex: 100,
          flexShrink: 0,
        }}
      >
        {/* Logo */}
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <div
            style={{
              width: 26,
              height: 26,
              borderRadius: 6,
              background: "#2ECC71",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            <svg width="12" height="12" viewBox="0 0 14 14" fill="none">
              <path d="M7 1L3 5.5h3v3L11 4H8V1z" fill="white" />
              <circle cx="7" cy="11" r="1.5" fill="white" opacity="0.6" />
            </svg>
          </div>
          <span
            style={{
              fontSize: 14,
              fontWeight: 600,
              color: T.text,
              letterSpacing: "-0.01em",
            }}
          >
            FixIt
          </span>
        </div>

        <div className="divider" />

        {/* Nav – UX-03: klik mijenja URL, Back/Forward rade */}
        <nav style={{ display: "flex", gap: 2 }}>
          {nav.map(({ path, label, Icon: Ic, badge }) => (
            <button
              key={path}
              onClick={() => handleTab(path)}
              className={`nav-btn ${isActive(path) ? "active" : ""}`}
              style={{ position: "relative" }}
            >
              <Ic /> {label}
              {badge > 0 && (
                <span
                  style={{
                    position: "absolute",
                    top: 2,
                    right: 2,
                    minWidth: 16,
                    height: 16,
                    borderRadius: 8,
                    background: "#E74C3C",
                    color: "#fff",
                    fontSize: 10,
                    fontWeight: 700,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    padding: "0 4px",
                    lineHeight: 1,
                  }}
                >
                  {badge > 99 ? "99+" : badge}
                </span>
              )}
            </button>
          ))}
        </nav>

        <div
          style={{
            marginLeft: "auto",
            display: "flex",
            alignItems: "center",
            gap: 12,
          }}
        >
          {/* User pill */}
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: 8,
              padding: "4px 10px 4px 6px",
              background: T.bgRaised,
              border: `1px solid ${T.line}`,
              borderRadius: 100,
            }}
          >
            <div
              style={{
                width: 22,
                height: 22,
                borderRadius: "50%",
                background: "rgba(46,204,113,0.2)",
                border: "1px solid rgba(46,204,113,0.4)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontSize: 10,
                fontWeight: 600,
                color: "#2ECC71",
              }}
            >
              {(user?.ime?.[0] || user?.email?.[0] || "U").toUpperCase()}
            </div>
            <span style={{ fontSize: 12, fontWeight: 500, color: T.text }}>
              {user?.ime || user?.email?.split("@")[0]}
            </span>
            {user?.uloga && user.uloga !== "GRADJANIN" && (
              <span
                style={{
                  fontSize: 9,
                  fontWeight: 600,
                  letterSpacing: "0.06em",
                  textTransform: "uppercase",
                  color: "#F39C12",
                  padding: "1px 5px",
                  borderRadius: 3,
                  background: "rgba(243,156,18,0.15)",
                  border: "1px solid rgba(243,156,18,0.3)",
                }}
              >
                {user.uloga}
              </span>
            )}
          </div>

          <button
            onClick={logout}
            className="btn-ghost"
            style={{ padding: "5px 10px", fontSize: 12 }}
          >
            <Icon.LogOut /> Odjava
          </button>
        </div>
      </header>

      <main style={{ flex: 1, padding: "40px 48px", width: "100%" }}>
        {children}
      </main>
    </div>
  );
}
