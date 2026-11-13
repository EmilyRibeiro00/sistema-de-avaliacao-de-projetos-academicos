import { useState, type FormEvent } from "react";
import { useQuery } from "@tanstack/react-query";

import { edicoesApi } from "../../api/edicoes";
import { Button } from "../../components/ui/Button";
import { Callout } from "../../components/ui/Callout";
import { SelectField, TextField } from "../../components/ui/Fields";
import { PageHeader } from "../../components/ui/PageHeader";
import { Title } from "../../components/ui/Title";

// Reservado para quando o backend expuser a importação de projetos do Sigaa (ImportacaoLoteTipo.PROJETOS_SIGAA).
// O modelo (ImportacaoLote/ImportacaoItem) já existe; falta o controller/service que de fato chama a API
// externa e grava os projetos. Nada neste formulário chama uma API além de listar as edições (essa é real).
export function ImportacaoPage() {
  const { data: edicoes } = useQuery({ queryKey: ["edicoes"], queryFn: edicoesApi.listar });
  const [enviado, setEnviado] = useState(false);

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setEnviado(true);
  }

  return (
    <>
      <PageHeader
        eyebrow="GESTÃO"
        title="Importação de projetos"
        description="Traga os projetos submetidos no Sigaa (ou outra fonte externa) para uma edição."
      />

      <Callout tone="warning" title="Ainda não existe endpoint no backend para isto">
        O modelo já tem <code>ImportacaoLote</code> e <code>ImportacaoItem</code> (com os status de processamento),
        mas nenhum controller chama uma API externa de verdade ainda. Este formulário é só o caminho reservado —
        guarda a URL e o token prontos para quando existir algo como{" "}
        <code>POST /api/importacoes/projetos-sigaa</code>.
      </Callout>

      <section className="card" style={{ marginTop: 18 }}>
        <div className="card-head">
          <div>
            <Title level={2}>Nova importação</Title>
            <p>A edição de destino já vem da API real; o resto é reservado para a integração futura.</p>
          </div>
        </div>
        <form onSubmit={onSubmit} className="modal-body">
          <div className="form-grid">
            <SelectField label="Edição de destino" name="edicaoId" required>
              <option value="">Selecione...</option>
              {edicoes?.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.nome} · {e.ano}
                </option>
              ))}
            </SelectField>
            <TextField label="Fonte" name="origem" placeholder="Ex.: Sigaa" defaultValue="Sigaa" />
          </div>
          <TextField label="URL da API externa" name="url" placeholder="https://sigaa.instituicao.edu.br/api/projetos" />
          <TextField label="Token de acesso" name="token" type="password" placeholder="Credencial da integração" />
          <Button type="submit" icon="upload">
            Importar projetos
          </Button>
          {enviado && (
            <Callout tone="info" title="Nada foi enviado">
              Essa integração ainda não existe no backend — quando existir, é só trocar o <code>onSubmit</code> desta
              página por uma chamada em <code>src/api/</code>, no mesmo padrão dos outros recursos.
            </Callout>
          )}
        </form>
      </section>
    </>
  );
}
