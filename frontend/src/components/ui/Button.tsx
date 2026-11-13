import type { ButtonHTMLAttributes } from "react";

import { Icon, type IconName } from "./Icon";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "danger" | "ghost" | "success";
  icon?: IconName;
}

// br-button é a classe oficial do DSGov (padrão gov.br); primary/secondary são variantes oficiais.
// danger/ghost/success não existem no DSGov — ficam como modificador próprio em cima da base br-button.
const VARIANT_CLASS: Record<NonNullable<ButtonProps["variant"]>, string> = {
  primary: "br-button primary",
  secondary: "br-button secondary",
  danger: "br-button danger",
  ghost: "br-button ghost",
  success: "br-button success",
};

export function Button({ children, variant = "primary", icon, className, type = "button", ...rest }: ButtonProps) {
  return (
    <button className={`${VARIANT_CLASS[variant]}${className ? ` ${className}` : ""}`} type={type} {...rest}>
      {icon && <Icon name={icon} />}
      <span className="text">{children}</span>
    </button>
  );
}
