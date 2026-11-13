import type { ReactNode } from "react";

import { Button } from "./Button";
import { Title } from "./Title";

interface ModalProps {
  title: string;
  children: ReactNode;
  onClose: () => void;
  action?: () => void;
  actionLabel?: string;
  actionDisabled?: boolean;
  /** Quando informado, o botão de confirmar vira um submit do <form id={submitFormId}> em vez de chamar `action`. */
  submitFormId?: string;
}

export function Modal({ title, children, onClose, action, actionLabel, actionDisabled, submitFormId }: ModalProps) {
  return (
    <div className="modal-backdrop" onMouseDown={onClose}>
      <div className="modal" onMouseDown={(e) => e.stopPropagation()}>
        <div className="modal-head">
          <Title level={2}>{title}</Title>
          <Button variant="ghost" onClick={onClose} type="button">
            Fechar
          </Button>
        </div>
        <div className="modal-body">{children}</div>
        <div className="modal-actions">
          <Button variant="secondary" onClick={onClose} type="button">
            Cancelar
          </Button>
          {submitFormId ? (
            <Button type="submit" form={submitFormId} disabled={actionDisabled}>
              {actionLabel ?? "Confirmar"}
            </Button>
          ) : (
            <Button onClick={action ?? onClose} disabled={actionDisabled} type="button">
              {actionLabel ?? "Confirmar"}
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}
