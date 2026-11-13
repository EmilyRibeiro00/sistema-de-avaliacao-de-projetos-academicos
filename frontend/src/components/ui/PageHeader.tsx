import type { ReactNode } from "react";

import { Title } from "./Title";

export function PageHeader({
  eyebrow,
  title,
  description,
  action,
}: {
  eyebrow: string;
  title: string;
  description: string;
  action?: ReactNode;
}) {
  return (
    <header className="page-header">
      <div>
        <span className="eyebrow">{eyebrow}</span>
        <Title>{title}</Title>
        <p>{description}</p>
      </div>
      {action}
    </header>
  );
}
