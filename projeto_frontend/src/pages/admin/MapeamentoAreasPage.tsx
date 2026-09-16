import { Button, Card, Group, Select, Stack, Text, Title } from "@mantine/core";
import { areasAliasPendentes } from "../../mock/data";

const AREAS_SISTEMA = [
  "Ciência da Computação",
  "Ciências Biológicas",
  "Saúde Coletiva",
  "Biblioteconomia",
  "Engenharia Civil",
];

export function MapeamentoAreasPage() {
  return (
    <Stack gap="lg" maw={680}>
      <div>
        <Title order={2}>Mapeamento de áreas pendentes</Title>
        <Text c="dimmed" size="sm">
          Textos de área vindos do Sigaa que ainda não foram associados a uma área de conhecimento do
          sistema. Enquanto pendente, o projeto correspondente não entra no sorteio de atribuição.
        </Text>
      </div>

      <Stack gap="sm">
        {areasAliasPendentes.map((alias) => (
          <Card key={alias.id} withBorder padding="md">
            <Group justify="space-between">
              <div>
                <Text size="xs" c="dimmed">Texto recebido do Sigaa</Text>
                <Text fw={500}>"{alias.textoOrigem}"</Text>
              </div>
              <Group>
                <Select
                  placeholder="Mapear para área do sistema"
                  data={AREAS_SISTEMA}
                  defaultValue={alias.areaSugerida}
                  w={260}
                />
                <Button size="sm">Salvar</Button>
              </Group>
            </Group>
          </Card>
        ))}
      </Stack>
    </Stack>
  );
}
