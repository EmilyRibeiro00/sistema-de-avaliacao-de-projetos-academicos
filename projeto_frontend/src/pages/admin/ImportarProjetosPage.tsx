import { useState } from "react";
import { Button, Card, Drawer, FileButton, Group, Stack, Table, Text, Title } from "@mantine/core";
import { IconUpload } from "@tabler/icons-react";
import { StatusBadge } from "../../components/StatusBadge";
import { itensLote1, lotesImportacao } from "../../mock/data";

export function ImportarProjetosPage() {
  const [loteSelecionado, setLoteSelecionado] = useState<number | null>(null);

  return (
    <Stack gap="lg">
      <Title order={2}>Importar projetos (Sigaa)</Title>

      <Card withBorder radius="md" padding="lg">
        <Group justify="space-between">
          <div>
            <Text fw={500}>Upload do JSON exportado do Sigaa</Text>
            <Text size="sm" c="dimmed">O sistema faz upsert por sigaaId — reenviar o mesmo arquivo não duplica projetos.</Text>
          </div>
          <FileButton onChange={() => {}} accept="application/json">
            {(props) => <Button leftSection={<IconUpload size={16} />} {...props}>Selecionar arquivo</Button>}
          </FileButton>
        </Group>
      </Card>

      <div>
        <Text fw={500} mb="sm">Histórico de importações</Text>
        <Table verticalSpacing="sm" withTableBorder highlightOnHover>
          <Table.Thead>
            <Table.Tr>
              <Table.Th>Data</Table.Th>
              <Table.Th>Itens</Table.Th>
              <Table.Th>Sucesso</Table.Th>
              <Table.Th>Erro</Table.Th>
              <Table.Th>Status</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {lotesImportacao.map((lote) => (
              <Table.Tr key={lote.id} style={{ cursor: "pointer" }} onClick={() => setLoteSelecionado(lote.id)}>
                <Table.Td>{lote.data}</Table.Td>
                <Table.Td>{lote.totalItens}</Table.Td>
                <Table.Td>{lote.totalSucesso}</Table.Td>
                <Table.Td>{lote.totalErro}</Table.Td>
                <Table.Td><StatusBadge status={lote.status} /></Table.Td>
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </div>

      <Drawer
        opened={loteSelecionado !== null}
        onClose={() => setLoteSelecionado(null)}
        title="Detalhe da importação"
        position="right"
        size="md"
      >
        <Stack gap="sm">
          {itensLote1.map((item) => (
            <Card key={item.id} withBorder padding="sm">
              <Group justify="space-between">
                <Text size="sm" fw={500}>{item.referencia}</Text>
                <StatusBadge status={item.status} />
              </Group>
              {item.mensagemErro && (
                <Text size="xs" c="dimmed" mt={4}>{item.mensagemErro}</Text>
              )}
            </Card>
          ))}
        </Stack>
      </Drawer>
    </Stack>
  );
}
