import type { ReactNode } from "react";

export type BadgeTone = "success" | "warning" | "danger" | "info" | "neutral";

// br-tag é a base oficial do DSGov pro formato (pill, tipografia); o DSGov não define variante de cor
// pra cada tom, então tag-{tone} é um modificador próprio por cima, usando as cores oficiais do padrão.
export function Badge({ children, tone = "neutral" }: { children: ReactNode; tone?: BadgeTone }) {
  return (
    <span className={`br-tag tag-${tone}`}>
      <i />
      {children}
    </span>
  );
}
