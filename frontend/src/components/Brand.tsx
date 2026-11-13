import { APP_NAME } from "../branding";
import { Icon } from "./ui/Icon";

export function Brand({ compact = false }: { compact?: boolean }) {
  return (
    <div className="brand">
      <span className="brand-mark">
        <Icon name="layers" size={compact ? 20 : 24} />
      </span>
      <span>
        <strong>{APP_NAME}</strong>
        {!compact && <small>Avaliação acadêmica</small>}
      </span>
    </div>
  );
}
