import T from "../styles/tokens";

export const STATUS_CFG = {
  "Novo":        { color: T.amber,  dim: T.amberDim,  border: T.amberBorder,  dot: T.amber  },
  "Dodijeljeno": { color: T.purple, dim: T.purpleDim, border: T.purpleBorder, dot: T.purple },
  "U radu":      { color: T.blue,   dim: T.blueDim,   border: T.blueBorder,   dot: T.blue   },
  "Rijeseno":    { color: T.green,  dim: T.greenDim,  border: T.greenBorder,  dot: T.green  },
  "Zatvoreno":   { color: T.textSub,dim: T.bgRaised,  border: T.line,         dot: T.textMuted },
};

export const PRIO_CFG = {
  "HITNO":   { color: T.red,    dim: T.redDim,    label: "Hitno"   },
  "VISOK":   { color: T.orange, dim: T.orangeDim, label: "Visok"   },
  "SREDNJI": { color: T.amber,  dim: T.amberDim,  label: "Srednji" },
  "NIZAK":   { color: T.green,  dim: T.greenDim,  label: "Nizak"   },
};

export const KATEGORIJE = [
  { id: 1, naziv: "Put / cesta" },
  { id: 2, naziv: "Javna rasvjeta" },
  { id: 3, naziv: "Vodovod" },
  { id: 4, naziv: "Zelenilo" },
  { id: 5, naziv: "Otpad" },
  { id: 6, naziv: "Saobraćaj" },
  { id: 7, naziv: "Ostalo" },
];
