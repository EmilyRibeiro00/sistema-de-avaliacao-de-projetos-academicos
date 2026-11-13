import { useRef, useState, type FormEvent } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { avaliadoresApi } from "../../api/avaliadores";
import { edicoesApi } from "../../api/edicoes";
import { ApiError } from "../../api/client";
import { Badge, type BadgeTone } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Callout } from "../../components/ui/Callout";
import { Modal } from "../../components/ui/Modal";
import { SelectField, TextField } from "../../components/ui/Fields";
import { PageHeader } from "../../components/ui/PageHeader";
import { Title } from "../../components/ui/Title";
import type {
  AvaliadorListResponse,
  CadastroAvaliadorCommand,
  EditarAvaliadorCommand,
  StatusAtivacao,
  VincularAvaliadorCommand,
} from "../../types/api";

const STATUS_LABEL: Record<StatusAtivacao, string> = {
  PENDENTE_ATIVACAO: "Pendente de ativação",
  ATIVO: "Ativo",
  INATIVO: "Removido",
};
const STATUS_TONE: Record<StatusAtivacao, BadgeTone> = {
  PENDENTE_ATIVACAO: "warning",
  ATIVO: "success",
  INATIVO: "neutral",
};

export function AvaliadoresPage() {
  const queryClient = useQueryClient();
  const { data: edicoes } = useQuery({ queryKey: ["edicoes"], queryFn: edicoesApi.listar });
  const { data: avaliadores, isLoading: carregandoLista } = useQuery({
    queryKey: ["avaliadores"],
    queryFn: avaliadoresApi.listar,
  });
  const [avaliadorEmEdicao, setAvaliadorEmEdicao] = useState<AvaliadorListResponse | null>(null);
  const [erroEdicao, setErroEdicao] = useState<string | null>(null);
  const [modalCadastroAberto, setModalCadastroAberto] = useState(false);

  const atualizar = useMutation({
    mutationFn: (vars: { id: number; comando: EditarAvaliadorCommand }) => avaliadoresApi.atualizar(vars.id, vars.comando),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["avaliadores"] });
      setAvaliadorEmEdicao(null);
    },
    onError: (e) => setErroEdicao(e instanceof ApiError ? e.message : "Não foi possível salvar as alterações."),
  });

  const remover = useMutation({
    mutationFn: (id: number) => avaliadoresApi.remover(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["avaliadores"] }),
    onError: (e) => window.alert(e instanceof ApiError ? e.message : "Não foi possível remover o avaliador."),
  });

  function onSubmitEditar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErroEdicao(null);
    if (!avaliadorEmEdicao) return;
    const dados = new FormData(event.currentTarget);
    atualizar.mutate({
      id: avaliadorEmEdicao.id,
      comando: {
        nome: String(dados.get("nome")),
        email: String(dados.get("email")),
        cpf: String(dados.get("cpf")),
        matricula: String(dados.get("matricula") || "") || undefined,
        siape: String(dados.get("siape") || "") || undefined,
        instituicaoNome: String(dados.get("instituicao") || "") || undefined,
        comiteNome: String(dados.get("comiteNome") || "") || undefined,
      },
    });
  }

  const [erro, setErro] = useState<string | null>(null);
  const formRef = useRef<HTMLFormElement>(null);
  const cadastrar = useMutation({
    mutationFn: (comando: CadastroAvaliadorCommand) => avaliadoresApi.cadastrar(comando),
    onSuccess: () => formRef.current?.reset(),
    onError: (e) => setErro(e instanceof ApiError ? e.message : "Não foi possível cadastrar o avaliador."),
  });

  const [erroVinculo, setErroVinculo] = useState<string | null>(null);
  const vincular = useMutation({
    mutationFn: (vars: { avaliadorId: number; comando: VincularAvaliadorCommand }) =>
      avaliadoresApi.vincular(vars.avaliadorId, vars.comando),
    onError: (e) => setErroVinculo(e instanceof ApiError ? e.message : "Não foi possível vincular o avaliador."),
  });

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErro(null);
    const dados = new FormData(event.currentTarget);
    cadastrar.mutate({
      nome: String(dados.get("nome")),
      email: String(dados.get("email")),
      cpf: String(dados.get("cpf")),
      matricula: String(dados.get("matricula") || "") || undefined,
      siape: String(dados.get("siape") || "") || undefined,
      instituicaoNome: String(dados.get("instituicao") || "") || undefined,
    });
  }

  function fecharModalCadastro() {
    setModalCadastroAberto(false);
    setErro(null);
    cadastrar.reset();
    formRef.current?.reset();
  }

  function onSubmitVinculo(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErroVinculo(null);
    const dados = new FormData(event.currentTarget);
    vincular.mutate({
      avaliadorId: Number(dados.get("avaliadorId")),
      comando: {
        edicaoId: Number(dados.get("edicaoId")),
        comiteNome: String(dados.get("comiteNome") || ""),
        campusNome: String(dados.get("campusNome") || ""),
      },
    });
  }

  return (
    <>
      <PageHeader
        eyebrow="GESTÃO"
        title="Avaliadores"
        description="Cadastre avaliadores para que entrem no sorteio automático de projetos."
        action={
          <Button variant="success" icon="userPlus" onClick={() => setModalCadastroAberto(true)}>
            Cadastrar avaliador
          </Button>
        }
      />

      <section className="card">
        <div className="card-head">
          <div>
            <Title level={2}>Todos os avaliadores</Title>
            <p>Edite os dados ou remova um avaliador (removido fica inativo, os dados não são apagados).</p>
          </div>
          <Badge tone="neutral">{avaliadores?.length ?? 0} avaliadores</Badge>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Avaliador</th>
                <th>Comitê</th>
                <th>Instituição</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {carregandoLista && (
                <tr>
                  <td colSpan={5}>Carregando...</td>
                </tr>
              )}
              {avaliadores?.length === 0 && (
                <tr>
                  <td colSpan={5}>Nenhum avaliador cadastrado ainda.</td>
                </tr>
              )}
              {avaliadores?.map((av) => (
                <tr key={av.id}>
                  <td>
                    <strong>{av.nome}</strong>
                    <small>{av.email}</small>
                  </td>
                  <td>{av.comiteNome ?? "—"}</td>
                  <td>{av.instituicaoNome ?? "—"}</td>
                  <td>
                    <Badge tone={STATUS_TONE[av.statusAtivacao]}>{STATUS_LABEL[av.statusAtivacao]}</Badge>
                  </td>
                  <td style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                    <Button
                      variant="ghost"
                      icon="edit"
                      onClick={() => {
                        setErroEdicao(null);
                        setAvaliadorEmEdicao(av);
                      }}
                    >
                      Editar
                    </Button>
                    {av.statusAtivacao !== "INATIVO" && (
                      <Button
                        variant="ghost"
                        icon="trash"
                        disabled={remover.isPending}
                        onClick={() => {
                          if (window.confirm(`Remover o avaliador "${av.nome}"? Ele fica inativo, mas os dados são mantidos.`)) {
                            remover.mutate(av.id);
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

      <section className="card" style={{ marginTop: 18 }}>
        <div className="card-head">
          <div>
            <Title level={2}>Vincular a uma edição</Title>
            <p>
              Sem isso o sorteio nunca encontra o avaliador — ele só considera quem está ativo na edição, no mesmo
              comitê e no mesmo campus do projeto. O comitê vale pra todas as edições do avaliador; o campus é só
              para esta edição. Vincular a uma edição nova libera automaticamente qualquer outra em que o
              avaliador estivesse ativo (só dá pra avaliar uma edição por vez).
            </p>
          </div>
        </div>
        <form onSubmit={onSubmitVinculo} className="modal-body">
          <div className="form-grid">
            <TextField label="ID do avaliador" name="avaliadorId" type="number" required />
            <SelectField label="Edição" name="edicaoId" required>
              <option value="">Selecione...</option>
              {edicoes?.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.nome} · {e.ano}
                </option>
              ))}
            </SelectField>
          </div>
          <div className="form-grid">
            <TextField label="Comitê" name="comiteNome" placeholder="Ex.: Comitê de Tecnologia" required />
            <TextField label="Campus" name="campusNome" placeholder="Ex.: Campus Bacanga" required />
          </div>
          <Button type="submit" variant="secondary" icon="userPlus" disabled={vincular.isPending}>
            {vincular.isPending ? "Vinculando..." : "Vincular"}
          </Button>
          {erroVinculo && <Callout tone="danger" title={erroVinculo} />}
          {vincular.isSuccess && (
            <Callout tone="success" title="Avaliador vinculado">
              Avaliador #{vincular.data.avaliadorId} está {vincular.data.status} na edição #{vincular.data.edicaoId}
              {vincular.data.comiteNome ? ` · comitê: ${vincular.data.comiteNome}` : ""}
              {vincular.data.campusNome ? ` · campus: ${vincular.data.campusNome}` : ""}.
            </Callout>
          )}
        </form>
      </section>

      {modalCadastroAberto && (
        <Modal
          title="Cadastrar avaliador"
          onClose={fecharModalCadastro}
          actionLabel={cadastrar.isPending ? "Cadastrando..." : "Cadastrar avaliador"}
          submitFormId="form-cadastro-avaliador"
          actionDisabled={cadastrar.isPending}
        >
          <form id="form-cadastro-avaliador" ref={formRef} onSubmit={onSubmit} className="form-stack">
            <div className="form-grid">
              <TextField label="Nome completo" name="nome" placeholder="Ex.: Maria da Silva" required />
              <TextField label="E-mail institucional" name="email" type="email" placeholder="nome@instituicao.edu.br" required />
            </div>
            <div className="form-grid">
              <TextField label="CPF" name="cpf" placeholder="000.000.000-00" required />
              <TextField label="Instituição" name="instituicao" placeholder="Ex.: UFXX (opcional)" />
            </div>
            <div className="form-grid">
              <TextField label="Matrícula" name="matricula" placeholder="Opcional" />
              <TextField label="SIAPE" name="siape" placeholder="Opcional" />
            </div>
            <Callout tone="info" title="O avaliador recebe uma senha padrão">
              Ele(a) precisa trocá-la no primeiro acesso. O backend não manda e-mail ainda (ver <code>EmailService</code>) —
              por isso a senha aparece aqui depois do cadastro, pra você copiar e repassar manualmente.
            </Callout>
            {erro && <Callout tone="danger" title={erro} />}
            {cadastrar.isSuccess && (
              <Callout tone="success" title={`${cadastrar.data.nome} cadastrado(a) com sucesso`}>
                Senha padrão (comunique a {cadastrar.data.email}, ele(a) troca no primeiro acesso):{" "}
                <code>{cadastrar.data.senhaTemporaria}</code>
              </Callout>
            )}
          </form>
        </Modal>
      )}

      {avaliadorEmEdicao && (
        <Modal
          title={`Editar "${avaliadorEmEdicao.nome}"`}
          onClose={() => setAvaliadorEmEdicao(null)}
          actionLabel={atualizar.isPending ? "Salvando..." : "Salvar alterações"}
          submitFormId="form-editar-avaliador"
          actionDisabled={atualizar.isPending}
        >
          <form id="form-editar-avaliador" onSubmit={onSubmitEditar} className="form-stack">
            <div className="form-grid">
              <TextField label="Nome completo" name="nome" defaultValue={avaliadorEmEdicao.nome} required />
              <TextField label="E-mail institucional" name="email" type="email" defaultValue={avaliadorEmEdicao.email} required />
            </div>
            <div className="form-grid">
              <TextField label="CPF" name="cpf" defaultValue={avaliadorEmEdicao.cpf} required />
              <TextField label="Instituição" name="instituicao" defaultValue={avaliadorEmEdicao.instituicaoNome ?? ""} />
            </div>
            <div className="form-grid">
              <TextField label="Matrícula" name="matricula" defaultValue={avaliadorEmEdicao.matricula ?? ""} />
              <TextField label="SIAPE" name="siape" defaultValue={avaliadorEmEdicao.siape ?? ""} />
            </div>
            <TextField
              label="Comitê"
              name="comiteNome"
              defaultValue={avaliadorEmEdicao.comiteNome ?? ""}
              placeholder="Deixe em branco para manter o atual"
              hint="Só troca o comitê se vier preenchido."
            />
            {erroEdicao && <Callout tone="danger" title={erroEdicao} />}
          </form>
        </Modal>
      )}
    </>
  );
}
