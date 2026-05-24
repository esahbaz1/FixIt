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
        border-color: #2ECC71 !important;
        box-shadow: 0 0 0 3px rgba(46,204,113,0.15) !important;
      }

      ::-webkit-scrollbar { width: 4px; height: 4px; }
      ::-webkit-scrollbar-track { background: transparent; }
      ::-webkit-scrollbar-thumb { background: ${T.bgActive}; border-radius: 4px; }

      @keyframes spin    { to { transform: rotate(360deg); } }
      @keyframes fadeUp  { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }
      @keyframes fadeIn  { from { opacity: 0; } to { opacity: 1; } }
      @keyframes toast   { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
      @keyframes blink   { 0%,100% { opacity: 1; } 50% { opacity: 0.3; } }
      @keyframes pulse   { 0%,100% { opacity: 1; } 50% { opacity: 0.5; } }

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
      .nav-btn.active { background: rgba(46,204,113,0.12); color: #2ECC71; }

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
        background: #2ECC71; color: #0F2A1A;
        font-size: 13px; font-weight: 600; cursor: pointer;
        transition: background 0.15s, transform 0.1s, box-shadow 0.15s;
      }
      .btn-prim:hover:not(:disabled) { background: #27AE60; transform: translateY(-1px); box-shadow: 0 4px 16px rgba(46,204,113,0.3); }
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
      .btn-ghost:disabled { opacity: 0.4; cursor: not-allowed; }

      .filter-chip {
        padding: 4px 12px; border-radius: 100px; border: 1px solid ${T.line};
        font-size: 12px; font-weight: 500; cursor: pointer;
        background: transparent; color: ${T.textSub};
        transition: all 0.12s;
      }
      .filter-chip:hover { border-color: ${T.lineHover}; color: ${T.text}; }
      .filter-chip.active {
        background: rgba(46,204,113,0.12); border-color: rgba(46,204,113,0.35); color: #2ECC71;
      }

      .input-field {
        width: 100%; padding: 9px 12px;
        background: ${T.bgRaised}; border: 1px solid ${T.line};
        border-radius: 8px; color: ${T.text}; font-size: 13px;
        transition: border-color 0.15s;
      }
      .input-field:hover { border-color: ${T.lineHover}; }

      .label {
        display: block; font-size: 12px; font-weight: 500;
        color: ${T.textSub}; letter-spacing: 0.06em;
        text-transform: uppercase; margin-bottom: 6px;
      }

      select.input-field {
        background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='10' height='10' viewBox='0 0 24 24' fill='none' stroke='%234D7A5C' stroke-width='2'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
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


      .grid-main { display: grid; grid-template-columns: 1fr 300px; gap: 16px; }
      .grid-main-wide { display: grid; grid-template-columns: 1fr 360px; gap: 16px; }
      .grid-main-narrow { display: grid; grid-template-columns: 1fr 280px; gap: 16px; }
      .grid-stats { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px; margin-bottom: 24px; }
      .grid-meta { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
      .grid-cat4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 6px; }
      .grid-prio { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }

      @media (max-width: 768px) {
        .grid-main, .grid-main-wide, .grid-main-narrow { grid-template-columns: 1fr; }
        .grid-stats { grid-template-columns: repeat(2, 1fr); }
        .grid-meta  { grid-template-columns: repeat(2, 1fr); }

   
        .nav-btn { padding: 6px 8px; font-size: 12px; }

        
        .tbl-row { padding: 0 12px; }
      }

      @media (max-width: 480px) {
        .grid-stats { grid-template-columns: 1fr; }
        .grid-meta  { grid-template-columns: 1fr; }
        .grid-cat4  { grid-template-columns: repeat(2, 1fr); }
        .grid-prio  { grid-template-columns: 1fr; }
        .grid-main, .grid-main-wide, .grid-main-narrow { grid-template-columns: 1fr; }

       
        .card { border-radius: 8px; }
      }

      
      .label { font-size: 12px; }

      
      .text-muted-accessible { color: #6FA882; }

     
      .app-header {
        height: 52px;
        background: rgba(15,61,40,0.97);
        backdrop-filter: blur(12px);
        border-bottom: 1px solid ${T.line};
        display: flex;
        align-items: center;
        padding: 0 24px;
        gap: 16px;
        position: sticky;
        top: 0;
        z-index: 100;
        flex-shrink: 0;
        overflow: hidden;
      }

      .app-main {
        flex: 1;
        padding: 40px 48px;
        width: 100%;
        max-width: 1600px;
        margin: 0 auto;
        box-sizing: border-box;
      }

      .desktop-nav { display: flex; }
      .hamburger-btn { display: none !important; }
      .mobile-nav-item { font-size: 14px; padding: 10px 14px; }

      
      @media (max-width: 900px) {
        .app-main { padding: 24px 20px; }
        .desktop-nav { display: none !important; }
        .hamburger-btn { display: flex !important; }
        .user-name { display: none; }
        .user-role { display: none; }
        .logout-text { display: none; }
        .logout-btn { padding: 5px 8px !important; }

        
        [style*="gridTemplateColumns"][style*="1fr 300px"],
        [style*="gridTemplateColumns"][style*="1fr 360px"],
        [style*="gridTemplateColumns"][style*="1fr 280px"] {
          grid-template-columns: 1fr !important;
        }
     
        [style*="repeat(4,1fr)"],
        [style*="repeat(4, 1fr)"] {
          grid-template-columns: repeat(2, 1fr) !important;
        }
       
        [style*="1fr 1fr"],
        [style*=""1fr 1fr""],
        [style*="gridTemplateColumns: "1fr 1fr""] {
          grid-template-columns: 1fr !important;
        }
      }

      
      @media (max-width: 600px) {
        .app-main { padding: 16px 12px; }
        .app-header { padding: 0 12px; gap: 10px; }
        .divider { display: none; }

     
        [style*="gridTemplateColumns"] {
          grid-template-columns: 1fr !important;
        }
       
        [style*="repeat(2,1fr)"],
        [style*="repeat(2, 1fr)"] {
          grid-template-columns: repeat(2, 1fr) !important;
        }

       
        .tbl-row { padding: 0 10px; height: auto !important; min-height: 52px; flex-wrap: wrap; }

       
        .card { border-radius: 8px; }
        .card > [style*="padding: "22px"] { padding: 16px !important; }

       
        .filter-chip { font-size: 11px; padding: 3px 9px; }

        
        [style*="flexWrap: "wrap""] { gap: 8px !important; }
      }


      .grid-main { display: grid; grid-template-columns: 1fr 300px; gap: 16px; }
      .grid-main-wide { display: grid; grid-template-columns: 1fr 360px; gap: 16px; }
      .grid-main-narrow { display: grid; grid-template-columns: 1fr 280px; gap: 16px; }
      .grid-stats { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px; margin-bottom: 24px; }
      .grid-meta { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
      .grid-cat4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 6px; }
      .grid-prio { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }

      @media (max-width: 900px) {
        .grid-main, .grid-main-wide, .grid-main-narrow { grid-template-columns: 1fr; }
        .grid-stats { grid-template-columns: repeat(2, 1fr); }
        .grid-meta  { grid-template-columns: repeat(2, 1fr); }
        .tbl-row { padding: 0 12px; }
      }
      @media (max-width: 600px) {
        .grid-stats { grid-template-columns: 1fr; }
        .grid-meta  { grid-template-columns: 1fr; }
        .grid-cat4  { grid-template-columns: repeat(2, 1fr); }
        .grid-prio  { grid-template-columns: 1fr; }
        .grid-main, .grid-main-wide, .grid-main-narrow { grid-template-columns: 1fr; }
      }
    `}</style>
  );
}
