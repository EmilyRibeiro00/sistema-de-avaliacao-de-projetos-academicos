import type { InputHTMLAttributes, ReactNode, SelectHTMLAttributes, TextareaHTMLAttributes } from "react";

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  hint?: string;
}

// br-input é a estrutura oficial do DSGov (padrão gov.br): <div class="br-input"><label/><input/></div>.
export function TextField({ label, hint, required, id, className, ...rest }: TextFieldProps) {
  const fieldId = id ?? label.toLowerCase().replace(/\s+/g, "-");
  return (
    <div className={`br-input${className ? ` ${className}` : ""}`}>
      <label htmlFor={fieldId}>
        {label}
        {required && <span className="required-mark"> *</span>}
      </label>
      <input id={fieldId} required={required} {...rest} />
      {hint && <small className="field-hint">{hint}</small>}
    </div>
  );
}

interface SelectFieldProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string;
  children: ReactNode;
}

// O componente br-select oficial é um widget com JS próprio (lista customizada); mantemos o <select>
// nativo — mais acessível e sem dependência de JS externo — só com a aparência (cor, borda, radius) do
// padrão gov.br via .br-select-native no CSS do app.
export function SelectField({ label, children, id, className, ...rest }: SelectFieldProps) {
  const fieldId = id ?? label.toLowerCase().replace(/\s+/g, "-");
  return (
    <div className={`br-select-native${className ? ` ${className}` : ""}`}>
      <label htmlFor={fieldId}>{label}</label>
      <select id={fieldId} {...rest}>
        {children}
      </select>
    </div>
  );
}

interface TextAreaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label: string;
}

export function TextArea({ label, id, required, className, ...rest }: TextAreaProps) {
  const fieldId = id ?? label.toLowerCase().replace(/\s+/g, "-");
  return (
    <div className={`br-textarea${className ? ` ${className}` : ""}`}>
      <label htmlFor={fieldId}>
        {label}
        {required && <span className="required-mark"> *</span>}
      </label>
      <textarea id={fieldId} rows={4} required={required} {...rest} />
    </div>
  );
}
