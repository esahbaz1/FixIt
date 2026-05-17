import { useState, useEffect } from "react";
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
import { setTokens, apiLogout, setKorisnikKontekst } from "./api/client";

export default function App() {
  const [screen, setScreen] = useState("login");
  const [auth, setAuth] = useState(null);
  const [activeTab, setActiveTab] = useState("dashboard");
  const [viewing, setViewing] = useState(null);
  const [toast, setToast] = useState(null);

  // Slušamo globalni event za prisilnu odjavu (istekao token)
  useEffect(() => {
    const handler = () => {
      setAuth(null);
      setScreen("login");
      showToast("Sesija je istekla. Prijavite se ponovo.", "error");
    };
    window.addEventListener("auth:logout", handler);
    return () => window.removeEventListener("auth:logout", handler);
  }, []);

  function showToast(msg, type = "success") {
    setToast({ msg, type });
  }

  function handleLogin(data) {
    // Pohrani tokene u memory store
    setTokens(data.token, data.refreshToken);
    setKorisnikKontekst(data.uloga, data.id);
    setAuth({ user: { id: data.id, email: data.email, uloga: data.uloga, ime: data.ime, prezime: data.prezime } });
    setScreen("app");
    setActiveTab("dashboard");
  }

  async function handleLogout() {
    await apiLogout();
    setAuth(null);
    setScreen("login");
  }

  const authCtx = auth ? {
    user: auth.user,
    logout: handleLogout,
    showToast,
  } : null;

  return (
    <>
      <GlobalStyles/>
      {toast && <Toast message={toast.msg} type={toast.type} onDone={() => setToast(null)}/>}

      {screen === "register" && (
        <RegisterPage
          onSuccess={() => { showToast("Nalog kreiran. Prijavite se."); setScreen("login"); }}
          switchToLogin={() => setScreen("login")}
        />
      )}

      {(screen === "login" || !auth) && screen !== "register" && (
        <LoginPage onLogin={handleLogin} switchToRegister={() => setScreen("register")}/>
      )}

      {screen === "app" && auth && (
        <AuthContext.Provider value={authCtx}>
          <AppShell
            activeTab={viewing ? "prijave" : activeTab}
            setActiveTab={t => { setViewing(null); setActiveTab(t); }}
          >
            {viewing ? (
              <PrijavaDetailPage
                prijava={viewing}
                onBack={() => setViewing(null)}
                onUpdated={(msg) => {
                  setViewing(null);
                  setActiveTab("prijave");
                  if (msg) showToast(msg);
                }}
              />
            ) : activeTab === "dashboard"    ? <Dashboard setActiveTab={setActiveTab}/> :
              activeTab === "prijave"        ? <PrijaveListPage onView={p => setViewing(p)}/> :
              activeTab === "nova"           ? <NovaPrijavaPage onSuccess={() => { showToast("Prijava uspješno kreirana!"); setActiveTab("prijave"); }}/> :
              activeTab === "notifikacije"   ? <NotifikacijePage/> :
              activeTab === "admin"          ? <AdminPage/> :
              <ProfilPage/>
            }
          </AppShell>
        </AuthContext.Provider>
      )}
    </>
  );
}
