import { useState, type FormEvent } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import dayjs from "dayjs";

import { edicoesApi } from "../../api/edicoes";
import { ApiError } from "../../api/client";
import { Badge, type BadgeTone } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Callout } from "../../components/ui/Callout";
import { TextField } from "../../components/ui/Fields";
import { Modal } from "../../components/ui/Modal";
import { PageHeader } from "../../components/ui/PageHeader";
import { Title } from "../../components/ui/Title";
import type { EdicaoCommand, EdicaoResponse, StatusEdicao } from "../../types/api";

const STATUS_LABEL: Record<StatusEdicao, string> = { PLANEJADA: "Planejada", ATIVA: "Ativa", ENCERRADA: "Encerrada" };
const STATUS_TONE: Record<StatusEdicao, BadgeTone> = { PLANEJADA: "info", ATIVA: "success", ENCERRADA: "neutral" };

function paraInstant(datetimeLocal: string): string {
  return new Date(datetimeLocal).toISOString();
}

function paraDatetimeLocal(iso: string): string {
  return dayjs(iso).format("YYYY-MM-DDTHH:mm");
}

export function EdicoesPage() {
  const queryClient = useQueryClient();
  const { data: edicoes, isLoading } = useQuery({ queryKey: ["edicoes"], queryFn: edicoesApi.listar });
  const [modalAberto, setModalAberto] = useState(false);
  const [edicaoEmEdicao, setEdicaoEmEdicao] = useState<EdicaoResponse | null>(null);
  const [erroForm, setErroForm] = useState<string | null>(null);
  const [acaoEmAndamento, setAcaoEmAndamento] = useState<number | null>(null);

  const criar = useMutation({
    mutationFn: (comando: EdicaoCommand) => edicoesApi.criar(comando),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["edicoes"] });
      setModalAberto(false);
    },
    onError: (e) => setErroForm(e instanceof ApiError ? e.message : "Não foi possível criar a edição."),
  });

  const atualizar = useMutation({
    mutationFn: (vars: { id: number; comando: EdicaoCommand }) => edicoesApi.atualizar(vars.id, vars.comando),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["edicoes"] });
      setEdicaoEmEdicao(null);
    },
    onError: (e) => setErroForm(e instanceof ApiError ? e.message : "Não foi possível salvar as alterações."),
  });

  const remover = useMutation({
    mutationFn: (id: number) => edicoesApi.remover(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["edicoes"] }),
    onError: (e) => window.alert(e instanceof ApiError ? e.message : "Não foi possível remover a edição."),
  });

  async function mudarStatus(edicao: EdicaoResponse, acao: "ativar" | "encerrar") {
    setAcaoEmAndamento(edicao.id);
    try {
      await (acao === "ativar" ? edicoesApi.ativar(edicao.id) : edicoesApi.encerrar(edicao.id));
      queryClient.invalidateQueries({ queryKey: ["edicoes"] });
    } catch (e) {
      window.alert(e instanceof ApiError ? e.message : "Não foi possível concluir a ação.");
    } finally {
      setAcaoEmAndamento(null);
    }
  }

  function comandoDoFormulario(dados: FormData): EdicaoCommand {
    return {
      nome: String(dados.get("nome")),
      ano: Number(dados.get("ano")),
      dataAbertura: paraInstant(String(dados.get("dataAbertura"))),
      dataFechamento: paraInstant(String(dados.get("dataFechamento"))),
      minAvaliadoresPorProjeto: dados.get("minAvaliadoresPorProjeto")
        ? Number(dados.get("minAvaliadoresPorProjeto"))
        : undefined,
      limiteMaxProjetosPorAvaliador: dados.get("limiteMaxProjetosPorAvaliador")
        ? Number(dados.get("limiteMaxProjetosPorAvaliador"))
        : undefined,
      limiteDivergenciaNotas: dados.get("limiteDivergenciaNotas")
        ? Number(dados.get("limiteDivergenciaNotas"))
        : undefined,
      escalaNotaMin: dados.get("escalaNotaMin") ? Number(dados.get("escalaNotaMin")) : undefined,
      escalaNotaMax: dados.get("escalaNotaMax") ? Number(dados.get("escalaNotaMax")) : undefined,
    };
  }

  function onSubmitCriar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErroForm(null);
    criar.mutate(comandoDoFormulario(new FormData(event.currentTarget)));
  }

  function onSubmitEditar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErroForm(null);
    if (!edicaoEmEdicao) return;
    atualizar.mutate({ id: edicaoEmEdicao.id, comando: comandoDoFormulario(new FormData(event.currentTarget)) });
  }

  function abrirEdicao(edicao: EdicaoResponse) {
    setErroForm(null);
    setEdicaoEmEdicao(edicao);
  }

  return (
    <>
      <PageHeader
        eyebrow="GESTÃO"
        title="Edições"
        description="Crie e acompanhe os ciclos de avaliação acadêmica."
        action={
          <Button icon="plus" onClick={() => setModalAberto(true)}>
            Nova edição
          </Button>
        }
      />

      <section className="card">
        <div className="card-head">
          <div>
            <Title level={2}>Todas as edições</Title>
            <p>Histórico e status dos ciclos de avaliação.</p>
          </div>
          <Badge tone="neutral">{edicoes?.length ?? 0} edições</Badge>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Edição</th>
                <th>Ano</th>
                <th>Período</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {isLoading && (
                <tr>
                  <td colSpan={5}>Carregando...</td>
                </tr>
              )}
              {edicoes?.length === 0 && (
                <tr>
                  <td colSpan={5}>Nenhuma edição cadastrada ainda.</td>
                </tr>
              )}
              {edicoes?.map((edicao) => (
                <tr key={edicao.id}>
                  <td>
                    <strong>{edicao.nome}</strong>
                    <small>Mínimo de {edicao.minAvaliadoresPorProjeto} avaliadores por projeto</small>
                  </td>
                  <td>{edicao.ano}</td>
                  <td>
                    {dayjs(edicao.dataAbertura).format("DD/MM/YYYY")} — {dayjs(edicao.dataFechamento).format("DD/MM/YYYY")}
                  </td>
                  <td>
                    <Badge tone={STATUS_TONE[edicao.status]}>{STATUS_LABEL[edicao.status]}</Badge>
                  </td>
                  <td style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                    <Button variant="ghost" icon="edit" onClick={() => abrirEdicao(edicao)}>
                      Editar
                    </Button>
                    {edicao.status === "PLANEJADA" && (
                      <Button
                        variant="secondary"
                        disabled={acaoEmAndamento === edicao.id}
                        onClick={() => mudarStatus(edicao, "ativar")}
                      >
                        Ativar
                      </Button>
                    )}
                    {edicao.status === "ATIVA" && (
                      <Button
                        variant="danger"
                        disabled={acaoEmAndamento === edicao.id}
                        onClick={() => {
                          if (window.confirm(`Encerrar a edição "${edicao.nome}"? Não é possível desfazer.`)) {
                            mudarStatus(edicao, "encerrar");
                          }
                        }}
                      >
                        Encerrar
                      </Button>
                    )}
                    {edicao.status !== "ATIVA" && (
                      <Button
                        variant="ghost"
                        icon="trash"
                        disabled={remover.isPending}
                        onClick={() => {
                          if (window.confirm(`Remover a edição "${edicao.nome}"? Ela deixa de aparecer na lista, mas os dados são mantidos.`)) {
                            remover.mutate(edicao.id);
                          }
                        }}
                      >
                        Remover
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {modalAberto && (
        <Modal
          title="Criar nova edição"
          onClose={() => setModalAberto(false)}
          actionLabel={criar.isPending ? "Criando..." : "Criar edição"}
          submitFormId="form-nova-edicao"
          actionDisabled={criar.isPending}
        >
          <form id="form-nova-edicao" onSubmit={onSubmitCriar} className="form-stack">
            <div className="form-grid">
              <TextField label="Nome da edição" name="nome" placeholder="Ex.: PAC Pesquisa & Inovação" required />
              <TextField label="Ano" name="ano" type="number" placeholder="2026" required />
              <TextField label="Data de abertura" name="dataAbertura" type="datetime-local" required />
              <TextField label="Data de fechamento" name="dataFechamento" type="datetime-local" required />
            </div>
            <div className="form-grid">
              <TextField
                label="Mínimo de avaliadores por projeto"
                name="minAvaliadoresPorProjeto"
                type="number"
                placeholder="3"
              />
              <TextField
                label="Limite de projetos por avaliador"
                name="limiteMaxProjetosPorAvaliador"
                type="number"
                placeholder="Sem limite"
              />
              <TextField
                label="Divergência máxima aceitável"
                name="limiteDivergenciaNotas"
                type="number"
                step="0.1"
                placeholder="Ex.: 2"
              />
              <TextField label="Escala de nota" name="escalaNotaMin" type="number" step="0.1" placeholder="0" />
            </div>
            <TextField label="Nota máxima da escala" name="escalaNotaMax" type="number" step="0.1" placeholder="10" />
            {erroForm && <Callout tone="danger" title={erroForm} />}
            <Callout tone="info" title="A edição será criada como planejada">
              Você poderá configurar critérios e ativá-la quando estiver pronta.
            </Callout>
          </form>
        </Modal>
      )}

      {edicaoEmEdicao && (
        <Modal
          title={`Editar "${edicaoEmEdicao.nome}"`}
          onClose={() => setEdicaoEmEdicao(null)}
          actionLabel={atualizar.isPending ? "Salvando..." : "Salvar alterações"}
          submitFormId="form-editar-edicao"
          actionDisabled={atualizar.isPending}
        >
          <form id="form-editar-edicao" onSubmit={onSubmitEditar} className="form-stack">
            <div className="form-grid">
              <TextField label="Nome da edição" name="nome" defaultValue={edicaoEmEdicao.nome} required />
              <TextField label="Ano" name="ano" type="number" defaultValue={edicaoEmEdicao.ano} required />
              <TextField
                label="Data de abertura"
                name="dataAbertura"
                type="datetime-local"
                defaultValue={paraDatetimeLocal(edicaoEmEdicao.dataAbertura)}
                required
              />
              <TextField
                label="Data de fechamento"
                name="dataFechamento"
                type="datetime-local"
                defaultValue={paraDatetimeLocal(edicaoEmEdicao.dataFechamento)}
                required
              />
            </div>
            <div className="form-grid">
              <TextField
                label="Mínimo de avaliadores por projeto"
                name="minAvaliadoresPorProjeto"
                type="number"
                defaultValue={edicaoEmEdicao.minAvaliadoresPorProjeto}
              />
              <TextField
                label="Limite de projetos por avaliador"
                name="limiteMaxProjetosPorAvaliador"
                type="number"
                defaultValue={edicaoEmEdicao.limiteMaxProjetosPorAvaliador ?? undefined}
                placeholder="Sem limite"
              />
              <TextField
                label="Divergência máxima aceitável"
                name="limiteDivergenciaNotas"
                type="number"
                step="0.1"
                defaultValue={edicaoEmEdicao.limiteDivergenciaNotas ?? undefined}
              />
              <TextField
                label="Escala de nota"
                name="escalaNotaMin"
                type="number"
                step="0.1"
                defaultValue={edicaoEmEdicao.escalaNotaMin}
              />
            </div>
            <TextField
              label="Nota máxima da escala"
              name="escalaNotaMax"
              type="number"
              step="0.1"
              defaultValue={edicaoEmEdicao.escalaNotaMax}
            />
            {erroForm && <Callout tone="danger" title={erroForm} />}
            {edicaoEmEdicao.status === "ENCERRADA" && (
              <Callout tone="warning" title="Edição encerrada é somente leitura">
                Não é possível salvar alterações nela.
              </Callout>
            )}
          </form>
        </Modal>
      )}
    </>
  );
}
