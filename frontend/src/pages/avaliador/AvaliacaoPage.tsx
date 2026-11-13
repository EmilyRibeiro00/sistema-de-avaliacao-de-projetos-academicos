import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useNavigate, useParams } from "react-router-dom";

import { avaliadorApi } from "../../api/avaliador";
import { ApiError } from "../../api/client";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Callout } from "../../components/ui/Callout";
import { Icon } from "../../components/ui/Icon";
import { PageHeader } from "../../components/ui/PageHeader";
import { Title } from "../../components/ui/Title";

export function AvaliacaoPage() {
  const { atribuicaoId } = useParams<{ atribuicaoId: string }>();
  const id = Number(atribuicaoId);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: detalhe, isLoading } = useQuery({ queryKey: ["avaliacao", id], queryFn: () => avaliadorApi.detalhar(id) });
  const [notas, setNotas] = useState<Record<string, string>>({});
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (!detalhe) return;
    const iniciais: Record<string, string> = {};
    detalhe.criterios.forEach((c) => {
      const valor = detalhe.notas[String(c.id)];
      if (valor !== undefined) iniciais[String(c.id)] = String(valor);
    });
    setNotas(iniciais);
  }, [detalhe]);

  function notasNumericas(): Record<string, number> {
    const resultado: Record<string, number> = {};
    for (const [id, valor] of Object.entries(notas)) {
      if (valor !== "") resultado[id] = Number(valor);
    }
    return resultado;
  }

  const salvarRascunho = useMutation({
    mutationFn: () => avaliadorApi.salvarRascunho(id, notasNumericas()),
    onSuccess: (novo) => queryClient.setQueryData(["avaliacao", id], novo),
    onError: (e) => setErro(e instanceof ApiError ? e.message : "Não foi possível salvar o rascunho."),
  });

  const enviar = useMutation({
    mutationFn: () => avaliadorApi.enviar(id, notasNumericas()),
    onSuccess: (novo) => queryClient.setQueryData(["avaliacao", id], novo),
    onError: (e) => setErro(e instanceof ApiError ? e.message : "Não foi possível enviar a avaliação."),
  });

  if (isLoading) return <p>Carregando...</p>;
  if (!detalhe) return <Callout tone="danger" title="Avaliação não encontrada" />;

  const enviada = detalhe.projeto.statusAvaliacao === "ENVIADA";

  return (
    <>
      <div className="back-link left" onClick={() => navigate("/avaliador/projetos")}>
        ← Voltar para meus projetos
      </div>
      <PageHeader
        eyebrow={`AVALIAÇÃO · PROJ-${detalhe.projeto.projetoId}`}
        title={detalhe.projeto.titulo}
        description={detalhe.projeto.comite ?? "Comitê não informado"}
        action={<Badge tone={enviada ? "success" : "info"}>{enviada ? "Enviada" : "Rascunho"}</Badge>}
      />

      <div className="evaluation-layout">
        <div>
          <section className="card project-summary">
            <div className="card-head">
              <Title level={2}>Resumo técnico</Title>
              <Badge tone="neutral">
                <Icon name="eye" size={13} /> Visão cega
              </Badge>
            </div>
            <p>{detalhe.projeto.conteudoTecnico}</p>
          </section>

          <section className="card evaluation-form">
            <div className="card-head">
              <div>
                <Title level={2}>Critérios de avaliação</Title>
                <p>Atribua uma nota dentro da escala para cada critério.</p>
              </div>
              <span className="completion">
                {Object.values(notas).filter((v) => v !== "").length}/{detalhe.criterios.length} preenchidos
              </span>
            </div>
            {detalhe.criterios.map((item, index) => (
              <div className="score-row" key={item.id}>
                <span>{index + 1}</span>
                <p>
                  <strong>{item.nome}</strong>
                  <small>Peso na nota final: {item.peso}%</small>
                </p>
                <label>
                  <small>NOTA</small>
                  <input
                    type="number"
                    step="0.1"
                    min={0}
                    max={10}
                    disabled={!detalhe.editavel}
                    value={notas[String(item.id)] ?? ""}
                    onChange={(e) => setNotas((prev) => ({ ...prev, [String(item.id)]: e.target.value }))}
                  />
                  <em>/ 10</em>
                </label>
              </div>
            ))}
          </section>
        </div>

        <aside className="evaluation-side">
          <div className="final-score">
            <small>NOTA FINAL PONDERADA</small>
            <strong>{detalhe.notaFinal !== null ? detalhe.notaFinal.toFixed(2).replace(".", ",") : "—"}</strong>
            <span>/ 10</span>
            <div className="progress">
              <i style={{ width: `${(detalhe.notaFinal ?? 0) * 10}%` }} />
            </div>
            <p>{detalhe.notaFinal !== null ? "Calculada pelo servidor." : "Calculada pelo servidor no envio."}</p>
          </div>

          {erro && <Callout tone="danger" title={erro} />}

          {!detalhe.editavel ? (
            <div className="sent-box">
              <Icon name="check" />
              <p>
                <strong>Avaliação enviada</strong>
                <small>Suas notas foram registradas e estão bloqueadas.</small>
              </p>
            </div>
          ) : (
            <>
              <Button variant="secondary" disabled={salvarRascunho.isPending} onClick={() => salvarRascunho.mutate()}>
                {salvarRascunho.isPending ? "Salvando..." : "Salvar rascunho"}
              </Button>
              <Button
                icon="check"
                disabled={enviar.isPending}
                onClick={() => {
                  if (window.confirm("Enviar avaliação? Depois do envio as notas ficam bloqueadas.")) enviar.mutate();
                }}
              >
                {enviar.isPending ? "Enviando..." : "Enviar avaliação"}
              </Button>
              <small className="submit-note">
                <Icon name="lock" size={14} /> Após o envio, as notas ficarão bloqueadas.
              </small>
            </>
          )}
        </aside>
      </div>
    </>
  );
}
