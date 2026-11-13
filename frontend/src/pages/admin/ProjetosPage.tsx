import { useRef, useState, type FormEvent } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { edicoesApi } from "../../api/edicoes";
import { projetosApi } from "../../api/projetos";
import { ApiError } from "../../api/client";
import { Badge, type BadgeTone } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { Callout } from "../../components/ui/Callout";
import { Modal } from "../../components/ui/Modal";
import { SelectField, TextArea, TextField } from "../../components/ui/Fields";
import { PageHeader } from "../../components/ui/PageHeader";
import { Title } from "../../components/ui/Title";
import type { CadastroProjetoCommand, EditarProjetoCommand, ProjetoResponse, StatusProjeto } from "../../types/api";

const STATUS_LABEL: Record<StatusProjeto, string> = {
  SUBMETIDO: "Submetido",
  EM_AVALIACAO: "Em avaliação",
  AVALIADO: "Avaliado",
};
const STATUS_TONE: Record<StatusProjeto, BadgeTone> = {
  SUBMETIDO: "neutral",
  EM_AVALIACAO: "info",
  AVALIADO: "success",
};

export function ProjetosAdminPage() {
  const queryClient = useQueryClient();
  const { data: edicoes } = useQuery({ queryKey: ["edicoes"], queryFn: edicoesApi.listar });
  const { data: projetos, isLoading: carregandoLista } = useQuery({
    queryKey: ["projetos"],
    queryFn: () => projetosApi.listar(),
  });

  const [erro, setErro] = useState<string | null>(null);
  const formRef = useRef<HTMLFormElement>(null);
  const cadastrar = useMutation({
    mutationFn: (comando: CadastroProjetoCommand) => projetosApi.cadastrar(comando),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["projetos"] });
      formRef.current?.reset();
    },
    onError: (e) => setErro(e instanceof ApiError ? e.message : "Não foi possível cadastrar o projeto."),
  });

  const [projetoEmEdicao, setProjetoEmEdicao] = useState<ProjetoResponse | null>(null);
  const [erroEdicao, setErroEdicao] = useState<string | null>(null);
  const atualizar = useMutation({
    mutationFn: (vars: { id: number; comando: EditarProjetoCommand }) => projetosApi.atualizar(vars.id, vars.comando),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["projetos"] });
      setProjetoEmEdicao(null);
    },
    onError: (e) => setErroEdicao(e instanceof ApiError ? e.message : "Não foi possível salvar as alterações."),
  });

  const remover = useMutation({
    mutationFn: (id: number) => projetosApi.remover(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["projetos"] }),
    onError: (e) => window.alert(e instanceof ApiError ? e.message : "Não foi possível remover o projeto."),
  });

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErro(null);
    const dados = new FormData(event.currentTarget);
    cadastrar.mutate({
      edicaoId: Number(dados.get("edicaoId")),
      sigaaId: String(dados.get("sigaaId") || "") || undefined,
      titulo: String(dados.get("titulo")),
      conteudoTecnico: String(dados.get("conteudoTecnico")),
      comiteNome: String(dados.get("comiteNome") || "") || undefined,
      campusNome: String(dados.get("campusNome") || "") || undefined,
      instituicaoNome: String(dados.get("instituicao") || "") || undefined,
      autorNome: String(dados.get("autorNome")),
      autorEmail: String(dados.get("autorEmail") || "") || undefined,
      autorCpf: String(dados.get("autorCpf") || "") || undefined,
    });
  }

  function onSubmitEditar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErroEdicao(null);
    if (!projetoEmEdicao) return;
    const dados = new FormData(event.currentTarget);
    atualizar.mutate({
      id: projetoEmEdicao.id,
      comando: {
        titulo: String(dados.get("titulo")),
        conteudoTecnico: String(dados.get("conteudoTecnico")),
        comiteNome: String(dados.get("comiteNome") || "") || undefined,
        campusNome: String(dados.get("campusNome") || "") || undefined,
        instituicaoNome: String(dados.get("instituicao") || "") || undefined,
      },
    });
  }

  return (
    <>
      <PageHeader
        eyebrow="GESTÃO"
        title="Projetos"
        description="Cadastre manualmente um projeto numa edição para poder sortear avaliadores e consolidar o resultado."
      />

      <Callout tone="info" title="Isto não é a importação do Sigaa">
        Não existe integração com o Sigaa ainda (ver página "Importação"). Este formulário cria o projeto direto no
        banco, com os dados que você digitar — útil pra testar o fluxo de atribuição e consolidação sem depender de
        uma importação real.
      </Callout>

      <section className="card" style={{ marginTop: 18 }}>
        <div className="card-head">
          <div>
            <Title level={2}>Todos os projetos</Title>
            <p>Edite os dados ou remova um projeto (removido fica inativo, os dados não são apagados).</p>
          </div>
          <Badge tone="neutral">{projetos?.length ?? 0} projetos</Badge>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Projeto</th>
                <th>Comitê</th>
                <th>Campus</th>
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
              {projetos?.length === 0 && (
                <tr>
                  <td colSpan={5}>Nenhum projeto cadastrado ainda.</td>
                </tr>
              )}
              {projetos?.map((p) => (
                <tr key={p.id}>
                  <td>
                    <strong>{p.titulo}</strong>
                    <small>#{p.id} · {p.sigaaId}</small>
                  </td>
                  <td>{p.comite ?? "—"}</td>
                  <td>{p.campus ?? "—"}</td>
                  <td>
                    <Badge tone={STATUS_TONE[p.status]}>{STATUS_LABEL[p.status]}</Badge>
                  </td>
                  <td style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                    <Button
                      variant="ghost"
                      icon="edit"
                      onClick={() => {
                        setErroEdicao(null);
                        setProjetoEmEdicao(p);
                      }}
                    >
                      Editar
                    </Button>
                    <Button
                      variant="ghost"
                      icon="trash"
                      disabled={remover.isPending}
                      onClick={() => {
                        if (window.confirm(`Remover o projeto "${p.titulo}"? Ele fica inativo, mas os dados são mantidos.`)) {
                          remover.mutate(p.id);
                        }
                      }}
                    >
                      Remover
                    </Button>
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
            <Title level={2}>Cadastrar projeto</Title>
            <p>O projeto entra como "Submetido"; o ID devolvido é o que você usa em Atribuições e Consolidação.</p>
          </div>
        </div>
        <form ref={formRef} onSubmit={onSubmit} className="modal-body">
          <div className="form-grid">
            <SelectField label="Edição" name="edicaoId" required>
              <option value="">Selecione...</option>
              {edicoes?.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.nome} · {e.ano}
                </option>
              ))}
            </SelectField>
            <TextField label="Identificador (opcional)" name="sigaaId" placeholder="Gerado automaticamente se vazio" />
          </div>
          <TextField label="Título" name="titulo" placeholder="Título do projeto" required />
          <TextArea label="Conteúdo técnico" name="conteudoTecnico" placeholder="Resumo técnico do projeto" required />
          <div className="form-grid">
            <TextField label="Comitê" name="comiteNome" placeholder="Ex.: Comitê de Tecnologia (opcional)" />
            <TextField label="Campus" name="campusNome" placeholder="Ex.: Campus Bacanga (opcional)" />
          </div>
          <TextField label="Instituição" name="instituicao" placeholder="Ex.: UFMA (opcional)" />
          <div className="form-grid">
            <TextField label="Nome do autor" name="autorNome" placeholder="Ex.: Maria Autora" required />
            <TextField label="E-mail do autor" name="autorEmail" type="email" placeholder="Opcional" />
          </div>
          <TextField label="CPF do autor" name="autorCpf" placeholder="Opcional — reaproveita a pessoa se já existir" />
          <Button type="submit" icon="plus" disabled={cadastrar.isPending}>
            {cadastrar.isPending ? "Cadastrando..." : "Cadastrar projeto"}
          </Button>
          {erro && <Callout tone="danger" title={erro} />}
          {cadastrar.isSuccess && (
            <Callout tone="success" title={`Projeto #${cadastrar.data.id} cadastrado`}>
              Status: {cadastrar.data.status}. Use o ID <code>{cadastrar.data.id}</code> em Atribuições ou Consolidação.
            </Callout>
          )}
        </form>
      </section>

      {projetoEmEdicao && (
        <Modal
          title={`Editar "${projetoEmEdicao.titulo}"`}
          onClose={() => setProjetoEmEdicao(null)}
          actionLabel={atualizar.isPending ? "Salvando..." : "Salvar alterações"}
          submitFormId="form-editar-projeto"
          actionDisabled={atualizar.isPending}
        >
          <form id="form-editar-projeto" onSubmit={onSubmitEditar} className="form-stack">
            <TextField label="Título" name="titulo" defaultValue={projetoEmEdicao.titulo} required />
            <TextArea label="Conteúdo técnico" name="conteudoTecnico" defaultValue={projetoEmEdicao.conteudoTecnico} required />
            <div className="form-grid">
              <TextField label="Comitê" name="comiteNome" defaultValue={projetoEmEdicao.comite ?? ""} />
              <TextField label="Campus" name="campusNome" defaultValue={projetoEmEdicao.campus ?? ""} />
            </div>
            <TextField label="Instituição" name="instituicao" defaultValue={projetoEmEdicao.instituicao ?? ""} />
            {erroEdicao && <Callout tone="danger" title={erroEdicao} />}
            {projetoEmEdicao.status === "AVALIADO" && (
              <Callout tone="warning" title="Este projeto já tem avaliações enviadas">
                Editar o comitê ou o campus não muda atribuições já feitas.
              </Callout>
            )}
          </form>
        </Modal>
      )}
    </>
  );
}
