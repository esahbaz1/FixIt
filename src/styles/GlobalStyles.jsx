import T from "./tokens";

export default function GlobalStyles() {
  return (
    <style>{`
      @import url('https://fonts.googleapis.com/css2?family=Geist:wght@300;400;500;600&family=Geist+Mono:wght@400;500&display=swap');

      *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

      html, body, #root {
        height: 100%;
        font-family: 'Geist', -apple-system, system-ui, sans-serif;
        background: ${T.bg};
        color: ${T.text};
        -webkit-font-smoothing: antialiased;
        font-size: 14px;
        line-height: 1.6;
      }

      input, textarea, select, button { font-family: inherit; }
      input::placeholder, textarea::placeholder { color: ${T.textMuted}; }

      input:focus, textarea:focus, select:focus {
        outline: none;
        border-color: ${T.blue} !important;
        box-shadow: 0 0 0 3px ${T.blueDim} !important;
      }

      ::-webkit-scrollbar { width: 4px; height: 4px; }
      ::-webkit-scrollbar-track { background: transparent; }
      ::-webkit-scrollbar-thumb { background: ${T.bgActive}; border-radius: 4px; }

      @keyframes spin    { to { transform: rotate(360deg); } }
      @keyframes fadeUp  { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }
      @keyframes fadeIn  { from { opacity: 0; } to { opacity: 1; } }
      @keyframes toast   { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
      @keyframes blink   { 0%,100% { opacity: 1; } 50% { opacity: 0.3; } }

      .row-enter { animation: fadeUp 0.25s ease both; }

      .nav-btn {
        display: flex; align-items: center; gap: 8px;
        padding: 6px 12px; border-radius: 6px; border: none;
        font-size: 13px; font-weight: 500; cursor: pointer;
        background: transparent; color: ${T.textSub};
        transition: color 0.15s, background 0.15s;
        white-space: nowrap;
      }
      .nav-btn:hover { background: ${T.bgRaised}; color: ${T.text}; }
      .nav-btn.active { background: ${T.bgActive}; color: ${T.text}; }

      .tbl-row {
        display: grid;
        align-items: center;
        padding: 0 24px;
        border-bottom: 1px solid ${T.line};
        cursor: pointer;
        transition: background 0.1s;
      }
      .tbl-row:hover { background: ${T.bgRaised}; }
      .tbl-row:last-child { border-bottom: none; }

      .chip {
        display: inline-flex; align-items: center; gap: 5px;
        padding: 2px 8px; border-radius: 4px; font-size: 11px;
        font-weight: 500; letter-spacing: 0.01em; white-space: nowrap;
        border: 1px solid;
      }

      .card {
        background: ${T.bgCard};
        border: 1px solid ${T.line};
        border-radius: 12px;
      }

      .btn-prim {
        display: inline-flex; align-items: center; justify-content: center; gap: 8px;
        padding: 9px 18px; border-radius: 8px; border: none;
        background: ${T.blue}; color: #fff;
        font-size: 13px; font-weight: 500; cursor: pointer;
        transition: background 0.15s, transform 0.1s, box-shadow 0.15s;
      }
      .btn-prim:hover:not(:disabled) { background: ${T.blueHover}; transform: translateY(-1px); box-shadow: 0 4px 16px rgba(79,126,255,0.3); }
      .btn-prim:active:not(:disabled) { transform: translateY(0); }
      .btn-prim:disabled { opacity: 0.4; cursor: not-allowed; }

      .btn-ghost {
        display: inline-flex; align-items: center; justify-content: center; gap: 8px;
        padding: 8px 16px; border-radius: 8px;
        border: 1px solid ${T.line}; background: transparent;
        color: ${T.textSub}; font-size: 13px; font-weight: 500; cursor: pointer;
        transition: all 0.15s;
      }
      .btn-ghost:hover { background: ${T.bgRaised}; border-color: ${T.lineHover}; color: ${T.text}; }

      .filter-chip {
        padding: 4px 12px; border-radius: 100px; border: 1px solid ${T.line};
        font-size: 12px; font-weight: 500; cursor: pointer;
        background: transparent; color: ${T.textSub};
        transition: all 0.12s;
      }
      .filter-chip:hover { border-color: ${T.lineHover}; color: ${T.text}; }
      .filter-chip.active {
        background: ${T.blueDim}; border-color: ${T.blueBorder}; color: ${T.blueHover};
      }

      .input-field {
        width: 100%; padding: 9px 12px;
        background: ${T.bgRaised}; border: 1px solid ${T.line};
        border-radius: 8px; color: ${T.text}; font-size: 13px;
        transition: border-color 0.15s;
      }
      .input-field:hover { border-color: ${T.lineHover}; }

      .label {
        display: block; font-size: 11px; font-weight: 500;
        color: ${T.textSub}; letter-spacing: 0.06em;
        text-transform: uppercase; margin-bottom: 6px;
      }

      select.input-field {
        background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='10' height='10' viewBox='0 0 24 24' fill='none' stroke='%234B5063' stroke-width='2'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
        background-repeat: no-repeat;
        background-position: calc(100% - 10px) center;
        appearance: none; padding-right: 32px;
      }

      .stat-card { animation: fadeUp 0.4s ease both; }
      .stat-card:nth-child(1) { animation-delay: 0.04s; }
      .stat-card:nth-child(2) { animation-delay: 0.08s; }
      .stat-card:nth-child(3) { animation-delay: 0.12s; }
      .stat-card:nth-child(4) { animation-delay: 0.16s; }
      .stat-card:nth-child(5) { animation-delay: 0.20s; }

      .divider { width: 1px; height: 16px; background: ${T.line}; flex-shrink: 0; }
    `}</style>
  );
}
