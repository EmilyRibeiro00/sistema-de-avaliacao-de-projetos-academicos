import { useState } from "react";
import { Alert, Button, Group, List, Loader, Modal, Stack, Table, Text, Title, TextInput } from "@mantine/core";
import { IconAlertTriangle, IconArrowsShuffle, IconCircleCheck, IconSearch } from "@tabler/icons-react";
import { useNavigate } from "react-router-dom";
import { StatusBadge } from "../../components/StatusBadge";
import { projetosAdmin } from "../../mock/data";

export function ProjetosPage() {
  const navigate = useNavigate();
  const [modalAberto, setModalAberto] = useState(false);
  const [atribuindo, setAtribuindo] = useState(false);
  const [atribuido, setAtribuido] = useState(false);

  function rodarAtribuicao() {
    setAtribuindo(true);
    setTimeout(() => {
      setAtribuindo(false);
      setAtribuido(true);
    }, 1500);
  }

  function fecharModal() {
    setModalAberto(false);
    setAtribuido(false);
  }

  return (
    <Stack gap="lg">
      <Group justify="space-between">
        <Title order={2}>Projetos da edição</Title>
        <Group>
          <TextInput placeholder="Buscar por título ou autor" leftSection={<IconSearch size={16} />} w={260} />
          <Button leftSection={<IconArrowsShuffle size={16} />} onClick={() => setModalAberto(true)}>
            Atribuição automática
          </Button>
        </Group>
      </Group>

      <Table verticalSpacing="sm" withTableBorder highlightOnHover>
        <Table.Thead>
          <Table.Tr>
            <Table.Th>Título</Table.Th>
            <Table.Th>Área</Table.Th>
            <Table.Th>Autor(es)</Table.Th>
            <Table.Th>Orientador(es)</Table.Th>
            <Table.Th>Avaliadores</Table.Th>
            <Table.Th>Status</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {projetosAdmin.map((p) => (
            <Table.Tr key={p.id} style={{ cursor: "pointer" }} onClick={() => navigate(`/admin/projetos/${p.id}`)}>
              <Table.Td maw={280}>
                <Text size="sm" lineClamp={1}>{p.titulo}</Text>
              </Table.Td>
              <Table.Td>{p.area}</Table.Td>
              <Table.Td>{[p.autor, ...p.coautores].join(", ")}</Table.Td>
              <Table.Td>{p.orientadores.join(", ")}</Table.Td>
              <Table.Td>{p.avaliacoesEnviadas}/{p.avaliadoresAtribuidos}</Table.Td>
              <Table.Td><StatusBadge status={p.status} /></Table.Td>
            </Table.Tr>
          ))}
        </Table.Tbody>
      </Table>

      <Modal opened={modalAberto} onClose={fecharModal} title="Atribuição automática">
        {!atribuido ? (
          <Stack gap="md">
            <Text size="sm" c="dimmed">
              Sorteia avaliadores para todos os projetos sem atribuição completa na edição ativa, respeitando
              área de conhecimento, conflito de interesse (orientador/coautor) e limite de carga por avaliador.
            </Text>
            <Group justify="flex-end">
              <Button variant="default" onClick={fecharModal}>Cancelar</Button>
              <Button
                leftSection={atribuindo ? <Loader size={14} color="white" /> : <IconArrowsShuffle size={16} />}
                disabled={atribuindo}
                onClick={rodarAtribuicao}
              >
                {atribuindo ? "Processando..." : "Rodar atribuição"}
              </Button>
            </Group>
          </Stack>
        ) : (
          <Stack gap="md">
            <Alert icon={<IconCircleCheck size={16} />} color="green" variant="light">
              42 projetos atribuídos com sucesso.
            </Alert>
            <Alert icon={<IconAlertTriangle size={16} />} color="orange" variant="light" title="Atenção necessária">
              3 projetos ficaram com pool de avaliadores insuficiente para a área e precisam de atribuição
              manual:
              <List size="sm" mt="xs">
                <List.Item>#105 — Reaproveitamento de água cinza em edificações escolares (Engenharia Civil)</List.Item>
                <List.Item>#112 — Semântica em textos jurídicos históricos (Letras)</List.Item>
                <List.Item>#118 — Nanomateriais para filtragem de água (Química)</List.Item>
              </List>
            </Alert>
            <Group justify="flex-end">
              <Button onClick={fecharModal}>Fechar</Button>
            </Group>
          </Stack>
        )}
      </Modal>
    </Stack>
  );
}
