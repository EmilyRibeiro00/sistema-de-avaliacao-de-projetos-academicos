import { useEffect, useState, type FormEvent } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { criteriosApi } from "../../api/criterios";
import { edicoesApi } from "../../api/edicoes";
import { ApiError } from "../../api/client";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Callout } from "../../components/ui/Callout";
import { Icon } from "../../components/ui/Icon";
import { SelectField, TextField } from "../../components/ui/Fields";
import { Modal } from "../../components/ui/Modal";
import { PageHeader } from "../../components/ui/PageHeader";
import { Title } from "../../components/ui/Title";
import type { CriterioCommand, CriterioView } from "../../types/api";

export function CriteriosPage() {
  const queryClient = useQueryClient();
  const { data: edicoes } = useQuery({ queryKey: ["edicoes"], queryFn: edicoesApi.listar });
  const [edicaoId, setEdicaoId] = useState<number | null>(null);
  const [modalAberto, setModalAberto] = useState(false);
  const [criterioEmEdicao, setCriterioEmEdicao] = useState<CriterioView | null>(null);
  const [erroForm, setErroForm] = useState<string | null>(null);

  useEffect(() => {
    if (edicaoId !== null || !edicoes?.length) return;
    const ativa = edicoes.find((e) => e.status === "ATIVA");
    setEdicaoId((ativa ?? edicoes[0]).id);
  }, [edicoes, edicaoId]);

  const edicaoSelecionada = edicoes?.find((e) => e.id === edicaoId) ?? null;

  const { data: criterios } = useQuery({
    queryKey: ["criterios", edicaoId],
    queryFn: () => criteriosApi.listar(edicaoId as number),
    enabled: edicaoId !== null,
  });

  const adicionar = useMutation({
    mutationFn: (comando: CriterioCommand) => criteriosApi.adicionar(edicaoId as number, comando),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["criterios", edicaoId] });
      setModalAberto(false);
    },
    onError: (e) => setErroForm(e instanceof ApiError ? e.message : "Não foi possível adicionar o critério."),
  });

  const atualizar = useMutation({
    mutationFn: (vars: { id: number; comando: CriterioCommand }) => criteriosApi.atualizar(vars.id, vars.comando),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["criterios", edicaoId] });
      setCriterioEmEdicao(null);
    },
    onError: (e) => setErroForm(e instanceof ApiError ? e.message : "Não foi possível salvar as alterações."),
  });

  const remover = useMutation({
    mutationFn: (criterioId: number) => criteriosApi.remover(criterioId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["criterios", edicaoId] }),
    onError: (e) => window.alert(e instanceof ApiError ? e.message : "Não foi possível remover o critério."),
  });

  const pesoTotal = criterios?.reduce((soma, c) => soma + c.peso, 0) ?? 0;

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErroForm(null);
    const dados = new FormData(event.currentTarget);
    adicionar.mutate({
      nome: String(dados.get("nome")),
      peso: Number(dados.get("peso")),
      ordem: dados.get("ordem") ? Number(dados.get("ordem")) : undefined,
    });
  }

  function onSubmitEditar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErroForm(null);
    if (!criterioEmEdicao) return;
    const dados = new FormData(event.currentTarget);
    atualizar.mutate({
      id: criterioEmEdicao.id,
      comando: {
        nome: String(dados.get("nome")),
        peso: Number(dados.get("peso")),
        ordem: dados.get("ordem") ? Number(dados.get("ordem")) : undefined,
      },
    });
  }

  if (!edicoes?.length) {
    return (
      <>
        <PageHeader eyebrow="GESTÃO" title="Critérios de avaliação" description="Defina os critérios e pesos usados no cálculo da nota final." />
        <Callout tone="info" title="Nenhuma edição cadastrada ainda">
          Crie uma edição em "Edições" antes de configurar os critérios.
        </Callout>
      </>
    );
  }

  return (
    <>
      <PageHeader
        eyebrow={edicaoSelecionada ? `${edicaoSelecionada.nome.toUpperCase()} · ${edicaoSelecionada.ano}` : "GESTÃO"}
        title="Critérios de avaliação"
        description="Defina os critérios e pesos usados no cálculo da nota final."
        action={
          <Button icon="plus" onClick={() => setModalAberto(true)} disabled={edicaoId === null}>
            Adicionar critério
          </Button>
        }
      />

      <div className="form-grid" style={{ marginBottom: 20, maxWidth: 420 }}>
        <SelectField label="Edição" value={edicaoId ?? ""} onChange={(e) => setEdicaoId(Number(e.target.value))}>
          {edicoes.map((e) => (
            <option key={e.id} value={e.id}>
              {e.nome} · {e.ano}
            </option>
          ))}
        </SelectField>
      </div>

      <div className="criteria-layout">
        <section className="card">
          <div className="card-head">
            <div>
              <Title level={2}>Composição da nota</Title>
              <p>A soma dos pesos define o peso relativo de cada critério na nota final.</p>
            </div>
            <Badge tone={edicaoSelecionada?.status === "ATIVA" ? "success" : "neutral"}>
              {edicaoSelecionada?.status === "ATIVA" ? "Edição ativa" : "Edição não ativa"}
            </Badge>
          </div>
          <div className="criteria-list">
            {criterios?.length === 0 && (
              <p style={{ padding: "16px 4px", color: "var(--muted)" }}>Nenhum critério cadastrado para esta edição.</p>
            )}
            {criterios?.map((item, index) => (
              <div className="criterion" key={item.id}>
                <span>{String(index + 1).padStart(2, "0")}</span>
                <p>
                  <strong>{item.nome}</strong>
                  <small>Critério da edição</small>
                </p>
                <div className="weight">{item.peso}%</div>
                <div style={{ display: "flex", gap: 6 }}>
                  <Button
                    variant="ghost"
                    icon="edit"
                    onClick={() => {
                      setErroForm(null);
                      setCriterioEmEdicao(item);
                    }}
                  >
                    Editar
                  </Button>
                  <Button
                    variant="ghost"
                    icon="trash"
                    disabled={remover.isPending}
                    onClick={() => {
                      if (window.confirm(`Remover o critério "${item.nome}"?`)) remover.mutate(item.id);
                    }}
                  >
                    Remover
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </section>
        <aside className="weight-card">
          <span className="metric-icon blue">
            <Icon name="chart" />
          </span>
          <small>PESO TOTAL</small>
          <strong>
            {pesoTotal}
            <span>%</span>
          </strong>
          <div className="progress">
            <i style={{ width: `${Math.min(pesoTotal, 100)}%` }} />
          </div>
          <p>
            <Icon name="check" size={16} /> {pesoTotal === 100 ? "Distribuição completa" : "Ainda não soma 100%"}
          </p>
          <small>Os critérios estão prontos para uso nas avaliações desta edição quando a soma chegar a 100%.</small>
        </aside>
      </div>

      {modalAberto && (
        <Modal
          title="Adicionar critério"
          onClose={() => setModalAberto(false)}
          actionLabel={adicionar.isPending ? "Adicionando..." : "Adicionar critério"}
          submitFormId="form-novo-criterio"
          actionDisabled={adicionar.isPending}
        >
          <form id="form-novo-criterio" onSubmit={onSubmit} className="form-stack">
            <TextField label="Nome do critério" name="nome" placeholder="Ex.: Potencial de impacto social" required />
            <TextField
              label="Peso (%)"
              name="peso"
              type="number"
              step="0.1"
              placeholder="0"
              hint={`Peso total atual: ${pesoTotal}%`}
              required
            />
            <TextField label="Ordem de exibição (opcional)" name="ordem" type="number" placeholder="1" />
            {erroForm && <Callout tone="danger" title={erroForm} />}
          </form>
        </Modal>
      )}

      {criterioEmEdicao && (
        <Modal
          title={`Editar "${criterioEmEdicao.nome}"`}
          onClose={() => setCriterioEmEdicao(null)}
          actionLabel={atualizar.isPending ? "Salvando..." : "Salvar alterações"}
          submitFormId="form-editar-criterio"
          actionDisabled={atualizar.isPending}
        >
          <form id="form-editar-criterio" onSubmit={onSubmitEditar} className="form-stack">
            <TextField label="Nome do critério" name="nome" defaultValue={criterioEmEdicao.nome} required />
            <TextField
              label="Peso (%)"
              name="peso"
              type="number"
              step="0.1"
              defaultValue={criterioEmEdicao.peso}
              hint={`Peso total atual: ${pesoTotal}%`}
              required
            />
            <TextField label="Ordem de exibição (opcional)" name="ordem" type="number" placeholder="1" />
            {erroForm && <Callout tone="danger" title={erroForm} />}
          </form>
        </Modal>
      )}
    </>
  );
}
