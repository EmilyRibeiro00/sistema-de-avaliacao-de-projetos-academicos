import type { ReactNode } from "react";

export function Title({ children, level = 1 }: { children: ReactNode; level?: 1 | 2 | 3 }) {
  return (
    <div className={`title title-${level}`} role="heading" aria-level={level}>
      {children}
    </div>
  );
}
