import { Alert, Avatar, Button, Card, Grid, Group, Progress, Stack, Text, Title } from "@mantine/core";
import { IconAlertTriangle, IconArrowLeft, IconArrowsShuffle } from "@tabler/icons-react";
import { useNavigate, useParams } from "react-router-dom";
import { StatusBadge } from "../../components/StatusBadge";
import { criterios, edicaoAtiva, notasProjeto103, projetosAdmin } from "../../mock/data";

export function ProjetoDetalhePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const projeto = projetosAdmin.find((p) => String(p.id) === id) ?? projetosAdmin[0];

  const temNotas = projeto.id === 103;
  const enviadas = temNotas ? notasProjeto103.filter((n) => n.status === "ENVIADA") : [];
  // Nota do projeto = média aritmética simples das notas finais enviadas (não confundir com a nota final de cada parecer)
  const notaDoProjeto = enviadas.length ? enviadas.reduce((s, n) => s + n.notaFinal, 0) / enviadas.length : 0;
  const notasFinais = enviadas.map((n) => n.notaFinal);
  const divergencia = notasFinais.length >= 2 ? Math.max(...notasFinais) - Math.min(...notasFinais) : 0;
  const divergenciaAlta = divergencia > edicaoAtiva.limiteDivergenciaNotas;

  return (
    <Stack gap="lg" maw={1000}>
      <Button variant="subtle" size="xs" leftSection={<IconArrowLeft size={14} />} onClick={() => navigate(-1)} w="fit-content">
        Voltar para projetos
      </Button>

      <Grid gap="lg">
        <Grid.Col span={{ base: 12, md: 5 }}>
          <Stack gap="md">
            <Card withBorder radius="md" padding="lg">
              <StatusBadge status={projeto.status} />
              <Title order={4} mt="xs" mb="sm">{projeto.titulo}</Title>
              <Stack gap={6}>
                <Group justify="space-between"><Text size="sm" c="dimmed">Autor</Text><Text size="sm">{projeto.autor}</Text></Group>
                {projeto.coautores.length > 0 && (
                  <Group justify="space-between"><Text size="sm" c="dimmed">Coautor(es)</Text><Text size="sm">{projeto.coautores.join(", ")}</Text></Group>
                )}
                <Group justify="space-between"><Text size="sm" c="dimmed">Orientador(es)</Text><Text size="sm">{projeto.orientadores.join(", ")}</Text></Group>
                <Group justify="space-between"><Text size="sm" c="dimmed">Instituição</Text><Text size="sm">{projeto.instituicao}</Text></Group>
                <Group justify="space-between"><Text size="sm" c="dimmed">Área</Text><Text size="sm">{projeto.area} · {projeto.subarea}</Text></Group>
              </Stack>
            </Card>

            <Card withBorder radius="md" padding="lg">
              <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb="sm">Resumo técnico</Text>
              <Text size="sm" c="dimmed" style={{ lineHeight: 1.6 }}>{projeto.resumoTecnico}</Text>
            </Card>
          </Stack>
        </Grid.Col>

        <Grid.Col span={{ base: 12, md: 7 }}>
          <Stack gap="md">
            <Card withBorder radius="md" padding="lg">
              <Group justify="space-between" mb="md">
                <Text fw={600}>Avaliadores atribuídos</Text>
                <Button size="xs" variant="light" leftSection={<IconArrowsShuffle size={14} />}>Reatribuir</Button>
              </Group>
              <Stack gap="sm">
                {(temNotas ? notasProjeto103 : []).map((n) => (
                  <Group key={n.avaliador} justify="space-between">
                    <Group gap="xs">
                      <Avatar size="sm" radius="xl" color="navy">
                        {n.avaliador.split(" ").slice(-2).map((p) => p[0]).join("")}
                      </Avatar>
                      <Text size="sm">{n.avaliador}</Text>
                    </Group>
                    <Group gap="xs">
                      {n.status === "ENVIADA" && <Text size="sm" fw={600} ff="monospace">{n.notaFinal.toFixed(2)}</Text>}
                      <StatusBadge status={n.status} />
                    </Group>
                  </Group>
                ))}
                {!temNotas && (
                  <Text size="sm" c="dimmed">
                    {projeto.avaliacoesEnviadas}/{projeto.avaliadoresAtribuidos} avaliações enviadas.
                  </Text>
                )}
              </Stack>
            </Card>

            {temNotas && (
              <Card withBorder radius="md" padding="lg">
                <Group justify="space-between" mb="md">
                  <Text fw={600}>Comparação de notas</Text>
                  <div style={{ textAlign: "right" }}>
                    <Text size="xs" c="dimmed">Nota do projeto</Text>
                    <Text fw={700} size="lg" ff="monospace">{notaDoProjeto.toFixed(2)}</Text>
                  </div>
                </Group>

                {divergenciaAlta && (
                  <Alert icon={<IconAlertTriangle size={16} />} color="red" variant="light" mb="md">
                    Divergência de {divergencia.toFixed(1)} pontos entre avaliadores — acima do limite
                    configurado ({edicaoAtiva.limiteDivergenciaNotas}). Considere solicitar um terceiro parecer.
                  </Alert>
                )}

                <Stack gap="lg">
                  {criterios.map((c) => (
                    <div key={c.id}>
                      <Text size="sm" fw={500} mb={6}>{c.nome} ({c.peso}%)</Text>
                      <Stack gap={8}>
                        {enviadas.map((n) => (
                          <div key={n.avaliador}>
                            <Group justify="space-between" mb={2}>
                              <Text size="xs" c="dimmed">{n.avaliador.replace("Prof. ", "").replace("Profa. ", "")}</Text>
                              <Text size="xs" fw={600}>{n.notas[c.nome]}</Text>
                            </Group>
                            <Progress value={n.notas[c.nome] * 10} color="navy" size="sm" />
                            {n.comentarios?.[c.nome] && (
                              <Text size="xs" c="dimmed" mt={2}>
                                {n.avaliador.replace("Prof. ", "").replace("Profa. ", "")}: {n.comentarios[c.nome]}
                              </Text>
                            )}
                          </div>
                        ))}
                      </Stack>
                    </div>
                  ))}
                </Stack>
              </Card>
            )}
          </Stack>
        </Grid.Col>
      </Grid>
    </Stack>
  );
}
