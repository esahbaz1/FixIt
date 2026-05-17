import T from "../styles/tokens";
import { STATUS_CFG, PRIO_CFG } from "../api/constants";

export function StatusChip({ status }) {
  const c = STATUS_CFG[status] || { color: T.textSub, dim: T.bgRaised, border: T.line };
  return (
    <span className="chip" style={{ background: c.dim, borderColor: c.border, color: c.color }}>
      <span style={{ width: 5, height: 5, borderRadius: "50%", background: c.color, flexShrink: 0 }}/>
      {status || "—"}
    </span>
  );
}

export function PrioChip({ priority }) {
  const c = PRIO_CFG[priority] || { color: T.textMuted, dim: T.bgRaised, label: "—" };
  return (
    <span className="chip" style={{ background: c.dim, borderColor: c.color + "40", color: c.color }}>
      {c.label}
    </span>
  );
}
