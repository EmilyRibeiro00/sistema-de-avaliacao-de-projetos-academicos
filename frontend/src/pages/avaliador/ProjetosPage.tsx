import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";

import { avaliadorApi } from "../../api/avaliador";
import { Badge, type BadgeTone } from "../../components/ui/Badge";
import { Icon } from "../../components/ui/Icon";
import { PageHeader } from "../../components/ui/PageHeader";
import { Title } from "../../components/ui/Title";
import type { ProjetoAvaliadorView } from "../../types/api";

const STATUS_LABEL: Record<string, string> = { PENDENTE: "Pendente", RASCUNHO: "Rascunho", ENVIADA: "Enviada" };
const STATUS_TONE: Record<string, BadgeTone> = { PENDENTE: "warning", RASCUNHO: "info", ENVIADA: "success" };

export function ProjetosPage() {
  const navigate = useNavigate();
  const { data: projetos, isLoading } = useQuery({ queryKey: ["avaliador-projetos"], queryFn: avaliadorApi.projetos });

  const contagem = (status: string) => projetos?.filter((p) => p.statusAvaliacao === status).length ?? 0;

  return (
    <>
      <PageHeader
        eyebrow="ÁREA DO AVALIADOR"
        title="Meus projetos"
        description="Avalie os projetos atribuídos a você. A identidade dos autores é protegida."
      />

      <div className="blind-banner">
        <span>
          <Icon name="eye" />
        </span>
        <p>
          <strong>Avaliação em visão cega</strong>
          <small>Autores, orientadores e instituições permanecem ocultos durante todo o processo.</small>
        </p>
        <Badge tone="success">Identidade protegida</Badge>
      </div>

      <div className="review-stats">
        <div>
          <strong>{projetos?.length ?? 0}</strong>
          <span>Projetos atribuídos</span>
        </div>
        <div>
          <strong className="amber-text">{contagem("PENDENTE")}</strong>
          <span>Pendente</span>
        </div>
        <div>
          <strong className="blue-text">{contagem("RASCUNHO")}</strong>
          <span>Em rascunho</span>
        </div>
        <div>
          <strong className="green-text">{contagem("ENVIADA")}</strong>
          <span>Enviada</span>
        </div>
      </div>

      {isLoading && <p>Carregando...</p>}
      {projetos?.length === 0 && <p style={{ color: "var(--muted)" }}>Nenhum projeto atribuído a você ainda.</p>}

      <div className="project-grid">
        {projetos?.map((projeto: ProjetoAvaliadorView) => (
          <article
            className="review-card"
            key={projeto.atribuicaoId}
            onClick={() => navigate(`/avaliador/atribuicoes/${projeto.atribuicaoId}`)}
          >
            <div className="review-card-top">
              <span className="project-code">PROJ-{projeto.projetoId}</span>
              <Badge tone={STATUS_TONE[projeto.statusAvaliacao]}>{STATUS_LABEL[projeto.statusAvaliacao]}</Badge>
            </div>
            <Title level={2}>{projeto.titulo}</Title>
            <p className="comite">
              <Icon name="folder" size={15} />
              {projeto.comite ?? "Comitê não informado"}
            </p>
            <div className="review-meta">
              <span>
                <p>
                  <small>CONTEÚDO TÉCNICO</small>
                  <strong>{projeto.conteudoTecnico.slice(0, 40)}...</strong>
                </p>
              </span>
              <span className={projeto.statusAvaliacao === "ENVIADA" ? "done" : ""}>
                <Icon name={projeto.statusAvaliacao === "ENVIADA" ? "check" : "arrow"} />
              </span>
            </div>
          </article>
        ))}
      </div>
    </>
  );
}
