import { useState, useEffect } from "react";
import { Routes, Route, Navigate, useNavigate } from "react-router-dom";
import GlobalStyles from "./styles/GlobalStyles";
import { AuthContext } from "./context/AuthContext";
import AppShell from "./components/AppShell";
import Toast from "./components/Toast";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import Dashboard from "./pages/Dashboard";
import PrijaveListPage from "./pages/PrijaveListPage";
import PrijavaDetailPage from "./pages/PrijavaDetailPage";
import NovaPrijavaPage from "./pages/NovaPrijavaPage";
import ProfilPage from "./pages/ProfilPage";
import NotifikacijePage from "./pages/NotifikacijePage";
import AdminPage from "./pages/AdminPage";
import Spinner from "./components/Spinner";
import {
  setTokens,
  apiLogout,
  setKorisnikKontekst,
  saveUserToStorage,
  loadUserFromStorage,
  loadRefreshTokenFromStorage,
  API_BASE,
} from "./api/client";

// Zaštićena ruta – preusmjerava na /login ako korisnik nije prijavljen
function PrivateRoute({ auth, children }) {
  if (auth === "loading") {
    return (
      <div style={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100vh" }}>
        <Spinner size={32} />
      </div>
    );
  }
  return auth ? children : <Navigate to="/login" replace />;
}

export default function App() {
  const navigate = useNavigate();
  // "loading" | null | { user }
  const [auth, setAuth] = useState("loading");
  const [toast, setToast] = useState(null);

  // Slušamo globalni event za prisilnu odjavu (istekao token)
  useEffect(() => {
    const handler = () => {
      setAuth(null);
      navigate("/login", { replace: true });
      showToast("Sesija je istekla. Prijavite se ponovo.", "error");
    };
    window.addEventListener("auth:logout", handler);
    return () => window.removeEventListener("auth:logout", handler);
  }, [navigate]);

  // UX-01 / CQ-04: Obnova sesije iz sessionStorage pri prvom pokretanju
  useEffect(() => {
    async function tryRestoreSession() {
      const savedUser = loadUserFromStorage();
      const savedRefreshToken = loadRefreshTokenFromStorage();

      if (!savedUser || !savedRefreshToken) {
        setAuth(null);
        return;
      }

      try {
        const res = await fetch(`${API_BASE}/api/auth/refresh`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ refreshToken: savedRefreshToken }),
        });

        if (!res.ok) throw new Error("Token nije valjan");

        const data = await res.json();
        setTokens(data.token, data.refreshToken || savedRefreshToken);
        setKorisnikKontekst(savedUser.uloga, savedUser.id);
        setAuth({ user: savedUser });
      } catch {
        setAuth(null);
      }
    }

    tryRestoreSession();
  }, []);

  function showToast(msg, type = "success") {
    setToast({ msg, type });
  }

  function handleLogin(data) {
    const userData = {
      id: data.id,
      email: data.email,
      uloga: data.uloga,
      ime: data.ime,
      prezime: data.prezime,
    };
    setTokens(data.token, data.refreshToken);
    setKorisnikKontekst(data.uloga, data.id);
    saveUserToStorage(userData);
    setAuth({ user: userData });
    navigate("/", { replace: true });
  }

  async function handleLogout() {
    await apiLogout();
    setAuth(null);
    navigate("/login", { replace: true });
  }

  const authCtx =
    auth && auth !== "loading"
      ? { user: auth.user, logout: handleLogout, showToast }
      : null;

  return (
    <>
      <GlobalStyles />
      {toast && (
        <Toast message={toast.msg} type={toast.type} onDone={() => setToast(null)} />
      )}

      {/* UX-03: Sve rute definirane ovdje — Back/Forward rade ispravno,
          URL-ovi su bookmarkable i shareable */}
      <Routes>
        {/* Javne rute */}
        <Route
          path="/login"
          element={
            auth && auth !== "loading" ? (
              <Navigate to="/" replace />
            ) : (
              <LoginPage
                onLogin={handleLogin}
                switchToRegister={() => navigate("/register")}
                loading={auth === "loading"}
              />
            )
          }
        />
        <Route
          path="/register"
          element={
            auth && auth !== "loading" ? (
              <Navigate to="/" replace />
            ) : (
              <RegisterPage
                onSuccess={() => {
                  showToast("Nalog kreiran. Prijavite se.");
                  navigate("/login");
                }}
                switchToLogin={() => navigate("/login")}
              />
            )
          }
        />

        {/* Zaštićene rute unutar AppShell-a */}
        <Route
          path="/*"
          element={
            <PrivateRoute auth={auth}>
              <AuthContext.Provider value={authCtx}>
                <AppShell>
                  <Routes>
                    <Route path="/" element={<Dashboard />} />
                    <Route path="/prijave" element={<PrijaveListPage />} />
                    <Route path="/prijave/:id" element={<PrijavaDetailPage />} />
                    <Route
                      path="/nova"
                      element={
                        <NovaPrijavaPage
                          onSuccess={() => {
                            showToast("Prijava uspješno kreirana!");
                            navigate("/prijave");
                          }}
                        />
                      }
                    />
                    <Route path="/notifikacije" element={<NotifikacijePage />} />
                    <Route path="/admin" element={<AdminPage />} />
                    <Route path="/profil" element={<ProfilPage />} />
                    <Route path="*" element={<Navigate to="/" replace />} />
                  </Routes>
                </AppShell>
              </AuthContext.Provider>
            </PrivateRoute>
          }
        />
      </Routes>
    </>
  );
}
