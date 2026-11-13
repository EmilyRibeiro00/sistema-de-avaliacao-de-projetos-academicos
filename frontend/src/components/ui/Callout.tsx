import type { ReactNode } from "react";

import { Icon, type IconName } from "./Icon";

export type CalloutTone = "info" | "warning" | "danger" | "success";

const toneIcon: Record<CalloutTone, IconName> = {
  info: "eye",
  warning: "alert",
  danger: "alert",
  success: "check",
};

// br-message é o alerta oficial do DSGov (padrão gov.br): tom via classe (info/warning/danger/success)
// e estrutura .icon + .content > .message-title/.message-body.
export function Callout({
  tone,
  title,
  children,
  icon,
}: {
  tone: CalloutTone;
  title: string;
  children?: ReactNode;
  icon?: IconName;
}) {
  return (
    <div className={`br-message ${tone}`} role="alert">
      <div className="icon">
        <Icon name={icon ?? toneIcon[tone]} />
      </div>
      <div className="content">
        <span className="message-title">{title}</span>
        {children && <span className="message-body">{children}</span>}
      </div>
    </div>
  );
}
