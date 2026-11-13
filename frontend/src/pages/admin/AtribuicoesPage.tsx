import { useState, type FormEvent } from "react";
import { useMutation } from "@tanstack/react-query";

import { atribuicoesApi } from "../../api/atribuicoes";
import { ApiError } from "../../api/client";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Callout } from "../../components/ui/Callout";
import { TextField } from "../../components/ui/Fields";
import { PageHeader } from "../../components/ui/PageHeader";
import { Title } from "../../components/ui/Title";
import type { AtribuicaoResponse, ResultadoAtribuicao } from "../../types/api";

function ResultadoCard({ resultado }: { resultado: ResultadoAtribuicao }) {
  return (
    <div className="assignment-summary" style={{ marginTop: 16 }}>
      <div>
        <small>PROJETO #{resultado.projetoId}</small>
        <strong>
          {resultado.jaAtribuidos + resultado.novosAtribuidos} <span>/ {resultado.jaAtribuidos + resultado.novosAtribuidos + resultado.faltantes}</span>
        </strong>
        <p>
          <span>+{resultado.novosAtribuidos}</span> novas atribuições
        </p>
      </div>
      <div>
        <small>FALTANTES</small>
        <strong className={resultado.faltantes > 0 ? "amber-text" : "green-text"}>{resultado.faltantes}</strong>
        <p>{resultado.faltantes > 0 ? "pool insuficiente" : "cobertura completa"}</p>
      </div>
      <div>
        <small>AVALIADORES SORTEADOS</small>
        <strong style={{ fontSize: 14 }}>{resultado.avaliadoresSorteados.join(", ") || "—"}</strong>
        {resultado.avisos.length > 0 && <p>{resultado.avisos.join("; ")}</p>}
      </div>
    </div>
  );
}

export function AtribuicoesPage() {
  const [projetoId, setProjetoId] = useState("");
  const [reatribuicaoResultado, setReatribuicaoResultado] = useState<AtribuicaoResponse | null>(null);

  const sortearEdicaoAtiva = useMutation({ mutationFn: atribuicoesApi.atribuirEdicaoAtiva });
  const sortearProjeto = useMutation({ mutationFn: (id: number) => atribuicoesApi.atribuirProjeto(id) });
  const reatribuir = useMutation({
    mutationFn: (vars: { atribuicaoId: number; novoAvaliadorId: number; motivo: string }) =>
      atribuicoesApi.reatribuir(vars.atribuicaoId, vars.novoAvaliadorId, vars.motivo),
    onSuccess: (resposta) => setReatribuicaoResultado(resposta),
  });

  function onSortearProjeto(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    sortearProjeto.mutate(Number(projetoId));
  }

  function onReatribuir(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const dados = new FormData(event.currentTarget);
    setReatribuicaoResultado(null);
    reatribuir.mutate({
      atribuicaoId: Number(dados.get("atribuicaoId")),
      novoAvaliadorId: Number(dados.get("novoAvaliadorId")),
      motivo: String(dados.get("motivo")),
    });
  }

  return (
    <>
      <PageHeader
        eyebrow="ATRIBUIÇÃO"
        title="Atribuição de avaliadores"
        description="Sorteio automático respeitando conflito de interesse, área e limite de carga — ou reatribuição manual com motivo."
        action={
          <Button icon="sparkle" disabled={sortearEdicaoAtiva.isPending} onClick={() => sortearEdicaoAtiva.mutate()}>
            {sortearEdicaoAtiva.isPending ? "Sorteando..." : "Sortear edição ativa inteira"}
          </Button>
        }
      />

      {sortearEdicaoAtiva.isError && (
        <Callout tone="danger" title={sortearEdicaoAtiva.error instanceof ApiError ? sortearEdicaoAtiva.error.message : "Não foi possível sortear a edição ativa."} />
      )}
      {sortearEdicaoAtiva.isSuccess && (
        <section className="card" style={{ marginBottom: 18, padding: "20px 23px" }}>
          <Title level={2}>Resultado do sorteio da edição ativa</Title>
          <p style={{ color: "var(--muted)", margin: "6px 0 0" }}>
            {sortearEdicaoAtiva.data.length} projeto(s) processado(s).
          </p>
          {sortearEdicaoAtiva.data.map((resultado) => (
            <ResultadoCard key={resultado.projetoId} resultado={resultado} />
          ))}
        </section>
      )}

      <section className="card" style={{ marginBottom: 18 }}>
        <div className="card-head">
          <div>
            <Title level={2}>Sortear um projeto específico</Title>
            <p>Sorteia avaliadores só para o projeto informado, até o mínimo configurado na edição.</p>
          </div>
        </div>
        <form onSubmit={onSortearProjeto} className="modal-body">
          <div className="form-grid">
            <TextField
              label="ID do projeto"
              type="number"
              value={projetoId}
              onChange={(e) => setProjetoId(e.target.value)}
              required
            />
          </div>
          <Button type="submit" icon="sparkle" disabled={sortearProjeto.isPending}>
            {sortearProjeto.isPending ? "Sorteando..." : "Sortear avaliadores"}
          </Button>
          {sortearProjeto.isError && (
            <Callout tone="danger" title={sortearProjeto.error instanceof ApiError ? sortearProjeto.error.message : "Não foi possível sortear."} />
          )}
          {sortearProjeto.isSuccess && <ResultadoCard resultado={sortearProjeto.data} />}
        </form>
      </section>

      <section className="card">
        <div className="card-head">
          <div>
            <Title level={2}>Reatribuição manual</Title>
            <p>Troca o avaliador de uma atribuição já existente. Motivo é obrigatório e fica registrado.</p>
          </div>
        </div>
        <form onSubmit={onReatribuir} className="modal-body">
          <div className="form-grid">
            <TextField label="ID da atribuição" name="atribuicaoId" type="number" required />
            <TextField label="ID do novo avaliador" name="novoAvaliadorId" type="number" required />
          </div>
          <TextField label="Motivo da reatribuição" name="motivo" placeholder="Explique por que esta alteração é necessária..." required />
          <Button type="submit" variant="secondary" icon="refresh" disabled={reatribuir.isPending}>
            {reatribuir.isPending ? "Reatribuindo..." : "Confirmar reatribuição"}
          </Button>
          {reatribuir.isError && (
            <Callout tone="danger" title={reatribuir.error instanceof ApiError ? reatribuir.error.message : "Não foi possível reatribuir."} />
          )}
          {reatribuicaoResultado && (
            <Callout tone="success" title="Reatribuição concluída">
              Atribuição #{reatribuicaoResultado.id} agora pertence ao avaliador #{reatribuicaoResultado.avaliadorId}.{" "}
              <Badge tone="info">{reatribuicaoResultado.origem}</Badge>
            </Callout>
          )}
        </form>
      </section>
    </>
  );
}
