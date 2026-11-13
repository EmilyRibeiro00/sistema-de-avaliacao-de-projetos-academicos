import { useState, type FormEvent } from "react";
import { useMutation } from "@tanstack/react-query";

import { consolidacaoApi } from "../../api/consolidacao";
import { ApiError } from "../../api/client";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Callout } from "../../components/ui/Callout";
import { Icon } from "../../components/ui/Icon";
import { TextField } from "../../components/ui/Fields";
import { PageHeader } from "../../components/ui/PageHeader";
import { Title } from "../../components/ui/Title";

export function ConsolidacaoPage() {
  const [projetoId, setProjetoId] = useState("");

  const consolidar = useMutation({ mutationFn: (id: number) => consolidacaoApi.consolidar(id) });
  const reabrir = useMutation({
    mutationFn: (vars: { avaliacaoId: number; motivo: string }) =>
      consolidacaoApi.reabrirAvaliacao(vars.avaliacaoId, vars.motivo),
  });

  function onConsultar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    consolidar.mutate(Number(projetoId));
  }

  function onReabrir(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const dados = new FormData(event.currentTarget);
    reabrir.mutate({ avaliacaoId: Number(dados.get("avaliacaoId")), motivo: String(dados.get("motivo")) });
  }

  const resultado = consolidar.data;

  return (
    <>
      <PageHeader
        eyebrow="CONSOLIDAÇÃO"
        title="Consolidação de projetos"
        description="Veja a nota final, a divergência entre avaliadores e reabra avaliações enviadas por engano."
      />

      <section className="card" style={{ marginBottom: 18 }}>
        <div className="card-head">
          <div>
            <Title level={2}>Consultar um projeto</Title>
            <p>Informe o ID do projeto para ver o resultado consolidado das avaliações.</p>
          </div>
        </div>
        <form onSubmit={onConsultar} className="modal-body">
          <div className="form-grid">
            <TextField label="ID do projeto" type="number" value={projetoId} onChange={(e) => setProjetoId(e.target.value)} required />
          </div>
          <Button type="submit" icon="chart" disabled={consolidar.isPending}>
            {consolidar.isPending ? "Consultando..." : "Consultar"}
          </Button>
          {consolidar.isError && (
            <Callout tone="danger" title={consolidar.error instanceof ApiError ? consolidar.error.message : "Não foi possível consultar."} />
          )}
        </form>
      </section>

      {resultado && (
        <>
          <div className="score-hero">
            <div>
              <small>NOTA DO PROJETO</small>
              <strong>{resultado.notaDoProjeto !== null ? resultado.notaDoProjeto.toFixed(2).replace(".", ",") : "—"}</strong>
              <p>de {resultado.avaliacoesEnviadas} avaliação(ões) enviada(s) de {resultado.avaliadoresAtribuidos} atribuída(s)</p>
            </div>
            <div className="score-ring">
              <span>
                {resultado.avaliado ? "100" : Math.round((resultado.avaliacoesEnviadas / Math.max(resultado.avaliadoresAtribuidos, 1)) * 100)}
                <small>%</small>
              </span>
            </div>
            <div className="divergence">
              <span className="metric-icon green">
                <Icon name={resultado.divergenciaAcimaDoLimite ? "alert" : "check"} />
              </span>
              <p>
                <small>DIVERGÊNCIA ENTRE AVALIADORES</small>
                <strong>{resultado.divergenciaAcimaDoLimite ? "Acima do limite" : "Dentro do limite"}</strong>
                <em>{resultado.divergencia !== null ? `Diferença de ${resultado.divergencia.toFixed(2)} ponto(s)` : "Sem dados suficientes"}</em>
              </p>
            </div>
          </div>

          <section className="card" style={{ marginBottom: 18 }}>
            <div className="card-head">
              <div>
                <Title level={2}>Notas por avaliador</Title>
                <p>Nota final enviada por cada avaliador atribuído a este projeto.</p>
              </div>
              <Badge tone="info">{resultado.notasPorAvaliador.length} avaliação(ões)</Badge>
            </div>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Avaliador</th>
                    <th>Nota final</th>
                  </tr>
                </thead>
                <tbody>
                  {resultado.notasPorAvaliador.length === 0 && (
                    <tr>
                      <td colSpan={2}>Nenhuma avaliação enviada ainda para este projeto.</td>
                    </tr>
                  )}
                  {resultado.notasPorAvaliador.map((n) => (
                    <tr key={n.avaliadorId}>
                      <td>
                        <strong>{n.avaliadorNome}</strong>
                        <small>Avaliador #{n.avaliadorId}</small>
                      </td>
                      <td>
                        <span className="note">{n.notaFinal.toFixed(1)}</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        </>
      )}

      <section className="card">
        <div className="card-head">
          <div>
            <Title level={2}>Reabrir avaliação</Title>
            <p>
              Reabre uma avaliação já enviada para o avaliador corrigir. Exige o ID da avaliação (não o do avaliador) —
              peça para o avaliador informar, já que esta tela de consolidação não expõe esse ID.
            </p>
          </div>
        </div>
        <form onSubmit={onReabrir} className="modal-body">
          <div className="form-grid">
            <TextField label="ID da avaliação" name="avaliacaoId" type="number" required />
          </div>
          <TextField label="Motivo da reabertura" name="motivo" placeholder="Informe o ajuste solicitado ao avaliador..." required />
          <Button type="submit" variant="secondary" icon="refresh" disabled={reabrir.isPending}>
            {reabrir.isPending ? "Reabrindo..." : "Reabrir avaliação"}
          </Button>
          {reabrir.isError && (
            <Callout tone="danger" title={reabrir.error instanceof ApiError ? reabrir.error.message : "Não foi possível reabrir."} />
          )}
          {reabrir.isSuccess && <Callout tone="success" title="Avaliação reaberta">O avaliador já pode enviar novas notas.</Callout>}
        </form>
      </section>
    </>
  );
}
