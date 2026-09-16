import { Badge } from "@mantine/core";

const CORES: Record<string, string> = {
  PENDENTE: "gray",
  RASCUNHO: "yellow",
  ENVIADA: "green",
  SUBMETIDO: "gray",
  EM_AVALIACAO: "blue",
  AVALIADO: "green",
  PLANEJADA: "gray",
  ATIVA: "blue",
  ENCERRADA: "gray",
  RECEBIDO: "gray",
  PROCESSANDO: "blue",
  CONCLUIDO: "green",
  CONCLUIDO_COM_ERROS: "yellow",
  FALHOU: "red",
  SUCESSO: "green",
  ERRO: "red",
  IGNORADO_DUPLICADO: "gray",
  PENDENTE_MAPEAMENTO: "yellow",
};

const ROTULOS: Record<string, string> = {
  PENDENTE: "Pendente",
  RASCUNHO: "Rascunho salvo",
  ENVIADA: "Enviada",
  SUBMETIDO: "Submetido",
  EM_AVALIACAO: "Em avaliação",
  AVALIADO: "Avaliado",
  PLANEJADA: "Planejada",
  ATIVA: "Ativa",
  ENCERRADA: "Encerrada",
  RECEBIDO: "Recebido",
  PROCESSANDO: "Processando",
  CONCLUIDO: "Concluído",
  CONCLUIDO_COM_ERROS: "Concluído com erros",
  FALHOU: "Falhou",
  SUCESSO: "Sucesso",
  ERRO: "Erro",
  IGNORADO_DUPLICADO: "Duplicado ignorado",
  PENDENTE_MAPEAMENTO: "Área pendente",
};

export function StatusBadge({ status }: { status: string }) {
  return (
    <Badge color={CORES[status] ?? "gray"} variant="light">
      {ROTULOS[status] ?? status}
    </Badge>
  );
}
