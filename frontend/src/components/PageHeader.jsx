import T from "../styles/tokens";

export default function PageHeader({ title, sub, action }) {
  return (
    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", marginBottom: 32 }}>
      <div>
        <h1 style={{ fontSize: 22, fontWeight: 600, color: T.text, letterSpacing: "-0.02em", marginBottom: 3 }}>{title}</h1>
        {sub && <p style={{ color: T.textSub, fontSize: 13 }}>{sub}</p>}
      </div>
      {action}
    </div>
  );
}
