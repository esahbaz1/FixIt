import { useEffect } from "react";
import T from "../styles/tokens";

export default function Toast({ message, type, onDone }) {
  useEffect(() => { const t = setTimeout(onDone, 3500); return () => clearTimeout(t); }, [onDone]);
  const ok = type === "success";
  return (
    <div style={{
      position: "fixed", bottom: 24, right: 24, zIndex: 9999,
      background: T.bgRaised, border: `1px solid ${ok ? T.greenBorder : T.redBorder}`,
      color: T.text, padding: "12px 18px", borderRadius: 10,
      display: "flex", alignItems: "center", gap: 10,
      fontSize: 13, fontWeight: 500,
      boxShadow: "0 8px 32px rgba(0,0,0,0.5)",
      animation: "toast 0.3s ease",
    }}>
      <span style={{
        width: 20, height: 20, borderRadius: "50%",
        background: ok ? T.greenDim : T.redDim,
        border: `1px solid ${ok ? T.greenBorder : T.redBorder}`,
        display: "flex", alignItems: "center", justifyContent: "center",
        fontSize: 10, color: ok ? T.green : T.red, flexShrink: 0,
      }}>
        {ok ? "✓" : "×"}
      </span>
      {message}
    </div>
  );
}
