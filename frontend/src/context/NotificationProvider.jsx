// frontend/src/context/NotificationProvider.jsx
import { useEffect, useState, useCallback, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { NotificationContext } from "./NotificationContext";
import { useSocket } from "../socket/useSocket";
import { apiCall } from "../api/client";
import { useAuth } from "./AuthContext";

const TIP_COLOR = {
  NOVA_PRIJAVA:    "#2ECC71",
  STATUS_PROMJENA: "#3498DB",
  NOVI_KOMENTAR:   "#9B59B6",
  DODIJELJENO:     "#F39C12",
  DODJELA_SLUZBI:  "#F39C12",
  DODJELA_RADNIKU: "#E67E22",
  UPOZORENJE:      "#E74C3C",
  RIJESENO:        "#2ECC71",
};

const TIP_LABEL = {
  NOVA_PRIJAVA:    "Nova prijava",
  STATUS_PROMJENA: "Promjena statusa",
  NOVI_KOMENTAR:   "Novi komentar",
  DODIJELJENO:     "Dodijeljena",
  DODJELA_SLUZBI:  "Dodijeljena službi",
  DODJELA_RADNIKU: "Dodijeljena vam prijava",
  UPOZORENJE:      "Upozorenje",
  RIJESENO:        "Riješeno",
};

const TIP_IKONA = {
  NOVA_PRIJAVA:    "📋",
  STATUS_PROMJENA: "🔄",
  NOVI_KOMENTAR:   "💬",
  DODIJELJENO:     "📌",
  DODJELA_SLUZBI:  "🏢",
  DODJELA_RADNIKU: "👷",
  UPOZORENJE:      "⚠️",
  RIJESENO:        "✅",
};

function LiveNotifToast({ data, onClose }) {
  const navigate = useNavigate();
  const color  = TIP_COLOR[data.tip]  || "#3498DB";
  const label  = TIP_LABEL[data.tip]  || data.tip;
  const ikona  = TIP_IKONA[data.tip]  || "🔔";

  function handleKlik() {
    onClose();
    if (data.prijavaId) navigate(`/prijave/${data.prijavaId}`);
  }

  return (
    <div
      onClick={data.prijavaId ? handleKlik : undefined}
      style={{
        position: "fixed", top: 20, right: 20, zIndex: 9999,
        display: "flex", alignItems: "flex-start", gap: 12,
        background: "rgba(12,30,20,0.97)",
        border: `1px solid ${color}50`,
        borderLeft: `4px solid ${color}`,
        borderRadius: 12, padding: "14px 16px",
        minWidth: 300, maxWidth: 400,
        boxShadow: "0 12px 40px rgba(0,0,0,0.5)",
        cursor: data.prijavaId ? "pointer" : "default",
        transition: "transform 0.15s",
      }}
    >
      {/* Ikona */}
      <div style={{ fontSize: 22, flexShrink: 0, marginTop: 1 }}>{ikona}</div>

      {/* Sadržaj */}
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{
          fontSize: 10, fontWeight: 700, textTransform: "uppercase",
          letterSpacing: "0.07em", color, marginBottom: 4,
        }}>
          {label}
        </div>
        <div style={{ fontSize: 13, fontWeight: 600, color: "#ecf0f1", lineHeight: 1.4, marginBottom: 3 }}>
          {data.naslov}
        </div>
        {data.tekst && (
          <div style={{ fontSize: 12, color: "#95a5a6", lineHeight: 1.5 }}>
            {data.tekst}
          </div>
        )}
        {data.prijavaId && (
          <div style={{ fontSize: 11, color: color, marginTop: 6, fontWeight: 600 }}>
            Klikni za pregled prijave →
          </div>
        )}
      </div>

      {/* Zatvori */}
      <button
        onClick={(e) => { e.stopPropagation(); onClose(); }}
        style={{ background: "none", border: "none", color: "#7f8c8d", cursor: "pointer", fontSize: 18, padding: "0 0 0 8px", flexShrink: 0 }}
      >×</button>
    </div>
  );
}

export function NotificationProvider({ children }) {
  const { user } = useAuth();
  const socketRef  = useSocket();
  const [notifCount, setNotifCount] = useState(0);
  const [liveToast,  setLiveToast]  = useState(null);
  const toastTimer = useRef(null);
  const prevUserIdRef = useRef(null);

  if (prevUserIdRef.current !== (user?.id ?? null)) {
    prevUserIdRef.current = user?.id ?? null;
  }

  useEffect(() => {
    if (!user?.id) return;
    apiCall(`/api/notifikacije/korisnik/${user.id}/broj-neprocitanih`)
      .then(d => setNotifCount(d?.brojNeprocitanih || 0))
      .catch(() => {});
  }, [user?.id]);

  useEffect(() => {
    if (user?.id) return;
    Promise.resolve().then(() => setNotifCount(0));
  }, [user?.id]);

  useEffect(() => {
    if (!user?.id) return;
    let registered = false;
    let stopped    = false;

    const handler = (notif) => {
      setNotifCount(prev => prev + 1);
      // Prikazi puni toast sa svim detaljima
      setLiveToast({
        naslov:    notif.naslov,
        tekst:     notif.tekst,
        tip:       notif.tip,
        prijavaId: notif.prijavaId,
      });
      if (toastTimer.current) clearTimeout(toastTimer.current);
      toastTimer.current = setTimeout(() => setLiveToast(null), 6000);
    };

    const tryRegister = () => {
      if (stopped || registered) return;
      const s = socketRef.current;
      if (!s) return;
      s.on("nova-notifikacija", handler);
      registered = true;
    };

    tryRegister();
    const interval = setInterval(tryRegister, 200);

    return () => {
      stopped = true;
      clearInterval(interval);
      const s = socketRef.current;
      if (s && registered) s.off("nova-notifikacija", handler);
      if (toastTimer.current) clearTimeout(toastTimer.current);
    };
  }, [user?.id]); // eslint-disable-line react-hooks/exhaustive-deps

  const resetCount  = useCallback(() => setNotifCount(0), []);
  const decrementBy = useCallback((n) => setNotifCount(prev => Math.max(0, prev - n)), []);

  return (
    <NotificationContext.Provider value={{ notifCount, resetCount, decrementBy, liveToast, setLiveToast }}>
      {children}
      {liveToast && <LiveNotifToast data={liveToast} onClose={() => setLiveToast(null)} />}
    </NotificationContext.Provider>
  );
}
