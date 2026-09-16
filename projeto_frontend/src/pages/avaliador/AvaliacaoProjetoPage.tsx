import { useMemo, useState } from "react";
import {
  Alert,
  Button,
  Card,
  Grid,
  Group,
  Modal,
  NumberInput,
  Slider,
  Stack,
  Text,
  Textarea,
  Title,
} from "@mantine/core";
import { IconAlertTriangle, IconArrowLeft, IconLock } from "@tabler/icons-react";
import { useNavigate, useParams } from "react-router-dom";
import { criterios, projetosAvaliador } from "../../mock/data";

export function AvaliacaoProjetoPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const projeto = projetosAvaliador.find((p) => String(p.id) === id) ?? projetosAvaliador[0];

  const [notas, setNotas] = useState<Record<string, number>>(
    Object.fromEntries(criterios.map((c) => [c.nome, projeto.minhasNotas?.[c.nome] ?? 7]))
  );
  const [comentarios, setComentarios] = useState<Record<string, string>>({});
  const [confirmarEnvioAberto, setConfirmarEnvioAberto] = useState(false);
  const [rascunhoSalvo, setRascunhoSalvo] = useState(false);

  const somatorioPesos = criterios.reduce((soma, c) => soma + c.peso, 0);
  const notaFinal = useMemo(() => {
    const soma = criterios.reduce((acc, c) => acc + notas[c.nome] * c.peso, 0);
    return soma / somatorioPesos;
  }, [notas, somatorioPesos]);

  const somenteLeitura = projeto.status === "ENVIADA";

  function salvarRascunho() {
    setRascunhoSalvo(true);
    setTimeout(() => setRascunhoSalvo(false), 3000);
  }

  return (
    <Stack gap="lg" maw={1100}>
      <Button variant="subtle" size="xs" leftSection={<IconArrowLeft size={14} />} onClick={() => navigate(-1)} w="fit-content">
        Voltar para projetos
      </Button>

      {somenteLeitura && (
        <Alert icon={<IconLock size={16} />} color="gray" variant="light">
          Esta avaliação já foi enviada e não pode mais ser editada. Só o administrador pode reabri-la.
        </Alert>
      )}

      <Grid gap="lg">
        <Grid.Col span={{ base: 12, md: 4 }}>
          <Stack gap="md" style={{ position: "sticky", top: 16 }}>
            <Card withBorder radius="md" padding="lg">
              <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={4}>Projeto</Text>
              <Title order={4} mb="sm">{projeto.titulo}</Title>
              <Stack gap={6}>
                <div>
                  <Text size="xs" c="dimmed">Área</Text>
                  <Text size="sm" fw={500}>{projeto.area}</Text>
                </div>
                <div>
                  <Text size="xs" c="dimmed">Subárea</Text>
                  <Text size="sm">{projeto.subarea}</Text>
                </div>
                <div>
                  <Text size="xs" c="dimmed">Prazo</Text>
                  <Text size="sm">{new Date(projeto.prazo).toLocaleDateString("pt-BR", { day: "2-digit", month: "long", year: "numeric" })}</Text>
                </div>
              </Stack>
            </Card>

            <Card withBorder radius="md" padding="lg">
              <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb="sm">Resumo técnico</Text>
              <Text size="sm" c="dimmed" style={{ lineHeight: 1.6 }}>{projeto.resumoTecnico}</Text>
            </Card>

            <Card withBorder radius="md" padding="lg" bg="navy.0">
              <Text size="xs" c="navy.8" tt="uppercase" fw={600} mb={4}>Nota final calculada</Text>
              <Text fz={36} fw={700} c="navy.9" ff="monospace" lh={1.1}>{notaFinal.toFixed(2)}</Text>
              <Text size="xs" c="navy.7" mb="sm">Calculada automaticamente pelos pesos</Text>
              <Stack gap={4}>
                {criterios.map((c) => (
                  <Group key={c.id} justify="space-between">
                    <Text size="xs" c="navy.7">{c.nome} ({c.peso}%)</Text>
                    <Text size="xs" fw={600} c="navy.9" ff="monospace">{notas[c.nome].toFixed(1)}</Text>
                  </Group>
                ))}
              </Stack>
            </Card>
          </Stack>
        </Grid.Col>

        <Grid.Col span={{ base: 12, md: 8 }}>
          <Stack gap="md">
            {criterios.map((criterio) => (
              <Card key={criterio.id} withBorder radius="md" padding="lg">
                <Group justify="space-between" align="flex-start" mb="xs">
                  <div>
                    <Group gap="xs">
                      <Text fw={600}>{criterio.nome}</Text>
                      <Text size="xs" px={8} py={2} bg="navy.0" c="navy.8" style={{ borderRadius: 999 }}>
                        Peso {criterio.peso}%
                      </Text>
                    </Group>
                    <Text size="sm" c="dimmed" mt={2}>{criterio.descricao}</Text>
                  </div>
                  <NumberInput
                    value={notas[criterio.nome]}
                    onChange={(v) => setNotas((n) => ({ ...n, [criterio.nome]: Number(v) || 0 }))}
                    min={0}
                    max={10}
                    step={0.5}
                    w={90}
                    size="xs"
                    disabled={somenteLeitura}
                  />
                </Group>

                <Group justify="space-between" mb={2}>
                  <Text size="xs" c="dimmed">0 — Insuficiente</Text>
                  <Text size="xs" c="dimmed">10 — Excelente</Text>
                </Group>
                <Slider
                  value={notas[criterio.nome]}
                  onChange={(v) => setNotas((n) => ({ ...n, [criterio.nome]: v }))}
                  min={0}
                  max={10}
                  step={0.5}
                  disabled={somenteLeitura}
                  marks={[0, 2, 4, 6, 8, 10].map((v) => ({ value: v, label: String(v) }))}
                  mb="lg"
                />

                <Text size="sm" fw={500} mb={4}>
                  Comentário / justificativa <Text span c="dimmed" fw={400}>(opcional)</Text>
                </Text>
                <Textarea
                  placeholder={`Justifique a nota para ${criterio.nome.toLowerCase()}...`}
                  autosize
                  minRows={2}
                  value={comentarios[criterio.nome] ?? ""}
                  onChange={(e) => setComentarios((c) => ({ ...c, [criterio.nome]: e.currentTarget.value }))}
                  disabled={somenteLeitura}
                />
              </Card>
            ))}

            {!somenteLeitura && (
              <Group grow>
                <Button variant="default" onClick={salvarRascunho}>
                  {rascunhoSalvo ? "Rascunho salvo ✓" : "Salvar rascunho"}
                </Button>
                <Button onClick={() => setConfirmarEnvioAberto(true)}>Enviar avaliação final</Button>
              </Group>
            )}
          </Stack>
        </Grid.Col>
      </Grid>

      <Modal
        opened={confirmarEnvioAberto}
        onClose={() => setConfirmarEnvioAberto(false)}
        title="Confirmar envio da avaliação"
      >
        <Stack gap="md">
          <Alert icon={<IconAlertTriangle size={16} />} color="orange" variant="light">
            Após o envio, a avaliação será registrada no sistema e não poderá ser alterada. Verifique suas
            notas antes de confirmar.
          </Alert>
          <Stack gap={4}>
            {criterios.map((c) => (
              <Group key={c.id} justify="space-between">
                <Text size="sm" c="dimmed">{c.nome} ({c.peso}%)</Text>
                <Text size="sm" fw={700} ff="monospace">{notas[c.nome].toFixed(1)}</Text>
              </Group>
            ))}
            <Group justify="space-between" pt="xs" style={{ borderTop: "1px solid var(--mantine-color-gray-2)" }}>
              <Text fw={600}>Nota final</Text>
              <Text fw={700} size="lg" ff="monospace" c="navy.8">{notaFinal.toFixed(2)}</Text>
            </Group>
          </Stack>
          <Group justify="flex-end">
            <Button variant="default" onClick={() => setConfirmarEnvioAberto(false)}>Cancelar</Button>
            <Button
              onClick={() => {
                setConfirmarEnvioAberto(false);
                navigate("/avaliador");
              }}
            >
              Confirmar envio
            </Button>
          </Group>
        </Stack>
      </Modal>
    </Stack>
  );
}
