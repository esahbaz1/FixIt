import { useState } from "react";
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

export default function App() {
  const [screen, setScreen] = useState("login");
  const [auth, setAuth] = useState(null);
  const [activeTab, setActiveTab] = useState("dashboard");
  const [viewing, setViewing] = useState(null);
  const [toast, setToast] = useState(null);

  function handleLogin(data) {
    setAuth({ token: data.token, user: { id: data.id, email: data.email, uloga: data.uloga } });
    setScreen("app");
  }

  const authCtx = auth ? {
    token: auth.token,
    user: auth.user,
    logout: () => { setAuth(null); setScreen("login"); },
  } : null;

  return (
    <>
      <GlobalStyles/>
      {toast && <Toast message={toast.msg} type={toast.type} onDone={() => setToast(null)}/>}

      {screen === "register" && (
        <RegisterPage
          onSuccess={() => { setToast({ msg: "Nalog kreiran. Prijavite se.", type: "success" }); setScreen("login"); }}
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
                onUpdated={() => { setViewing(null); setActiveTab("prijave"); }}
              />
            ) : activeTab === "dashboard" ? <Dashboard setActiveTab={setActiveTab}/> :
              activeTab === "prijave"   ? <PrijaveListPage onView={p => setViewing(p)}/> :
              activeTab === "nova"      ? <NovaPrijavaPage onSuccess={() => setActiveTab("prijave")}/> :
              <ProfilPage/>
            }
          </AppShell>
        </AuthContext.Provider>
      )}
    </>
  );
}
