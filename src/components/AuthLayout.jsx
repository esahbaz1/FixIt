import T from "../styles/tokens";

export default function AuthLayout({ children }) {
  return (
    <div style={{ minHeight: "100vh", display: "flex" }}>
      {/* Left panel */}
      <div style={{
        flex: "0 0 500px",
        background: T.bgCard,
        borderRight: `1px solid ${T.line}`,
        display: "flex", flexDirection: "column",
        padding: "56px 60px",
        position: "relative", overflow: "hidden",
      }}>
        {/* Subtle noise */}
        <svg style={{ position: "absolute", inset: 0, width: "100%", height: "100%", opacity: 0.015, pointerEvents: "none" }}>
          <filter id="noise"><feTurbulence type="fractalNoise" baseFrequency="0.9" numOctaves="4" stitchTiles="stitch"/></filter>
          <rect width="100%" height="100%" filter="url(#noise)"/>
        </svg>

        {/* Grid lines */}
        <svg style={{ position: "absolute", inset: 0, width: "100%", height: "100%", opacity: 1, pointerEvents: "none" }}>
          <defs>
            <pattern id="grid" width="48" height="48" patternUnits="userSpaceOnUse">
              <path d="M 48 0 L 0 0 0 48" fill="none" stroke="rgba(255,255,255,0.03)" strokeWidth="1"/>
            </pattern>
          </defs>
          <rect width="100%" height="100%" fill="url(#grid)"/>
        </svg>

        {/* Accent line top */}
        <div style={{ position: "absolute", top: 0, left: 0, right: 0, height: 1, background: `linear-gradient(90deg, transparent, ${T.blue}60, transparent)` }}/>

        <div style={{ position: "relative", zIndex: 1, display: "flex", flexDirection: "column", height: "100%" }}>
          {/* Logo */}
          <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 72 }}>
            <div style={{
              width: 30, height: 30, borderRadius: 8,
              background: T.blue,
              display: "flex", alignItems: "center", justifyContent: "center",
            }}>
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M7 1L3 5.5h3v3L11 4H8V1z" fill="white"/>
                <circle cx="7" cy="11" r="1.5" fill="white" opacity="0.6"/>
              </svg>
            </div>
            <span style={{ fontSize: 15, fontWeight: 600, color: T.text, letterSpacing: "-0.01em" }}>FixIt</span>
          </div>

          {/* Headline */}
          <div style={{ flex: 1 }}>
            <div style={{
              display: "inline-block",
              background: T.blueDim, border: `1px solid ${T.blueBorder}`,
              borderRadius: 4, padding: "3px 10px",
              fontSize: 11, fontWeight: 500, color: T.blue,
              letterSpacing: "0.06em", textTransform: "uppercase",
              marginBottom: 24,
            }}>
              Komunalne prijave
            </div>

            <h1 style={{
              fontSize: 36, fontWeight: 300, color: T.text,
              lineHeight: 1.2, letterSpacing: "-0.03em",
              marginBottom: 20,
            }}>
              Brže rješavanje<br/>
              <span style={{ fontWeight: 600 }}>komunalnih problema</span>
            </h1>

            <p style={{ color: T.textSub, fontSize: 14, lineHeight: 1.7, maxWidth: 340 }}>
              Platforma koja povezuje građane s komunalnim službama. Prijavite, pratite i pratite status prijava u realnom vremenu.
            </p>

            {/* Feature list */}
            <div style={{ marginTop: 48, display: "flex", flexDirection: "column", gap: 14 }}>
              {[
                ["Transparentno praćenje", "Status prijave vidljiv u svakom trenutku"],
                ["Prioritizacija prijava", "Hitne situacije odmah eskaliraju"],
                ["Kategorizacija", "Precizan routing do nadležnih službi"],
              ].map(([title, desc]) => (
                <div key={title} style={{ display: "flex", gap: 14, alignItems: "flex-start" }}>
                  <div style={{
                    width: 18, height: 18, borderRadius: 4,
                    background: T.greenDim, border: `1px solid ${T.greenBorder}`,
                    display: "flex", alignItems: "center", justifyContent: "center",
                    flexShrink: 0, marginTop: 2,
                  }}>
                    <svg width="8" height="8" viewBox="0 0 8 8" fill="none">
                      <polyline points="1,4 3,6 7,2" stroke={T.green} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                  </div>
                  <div>
                    <div style={{ fontSize: 13, fontWeight: 500, color: T.text, marginBottom: 1 }}>{title}</div>
                    <div style={{ fontSize: 12, color: T.textSub }}>{desc}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Bottom bar */}
          <div style={{
            paddingTop: 32, borderTop: `1px solid ${T.line}`,
            display: "flex", gap: 20,
          }}>
            {[["1.2k+", "Prijava riješeno"], ["94%", "Stopa rješavanja"], ["<48h", "Prosječno vrijeme"]].map(([v, l]) => (
              <div key={l}>
                <div style={{ fontSize: 18, fontWeight: 600, color: T.text, letterSpacing: "-0.02em" }}>{v}</div>
                <div style={{ fontSize: 11, color: T.textMuted }}>{l}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right form */}
      <div style={{
        flex: 1,
        display: "flex", alignItems: "center", justifyContent: "center",
        padding: "48px 60px",
      }}>
        <div style={{ width: "100%", maxWidth: 380 }}>
          {children}
        </div>
      </div>
    </div>
  );
}
