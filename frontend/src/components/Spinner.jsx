import T from "../styles/tokens";

export default function Spinner({ size = 18, color = T.blue }) {
  return (
    <span style={{
      width: size, height: size, borderRadius: "50%",
      border: `1.5px solid ${color}28`,
      borderTop: `1.5px solid ${color}`,
      display: "inline-block",
      animation: "spin 0.65s linear infinite",
      flexShrink: 0,
    }}/>
  );
}
