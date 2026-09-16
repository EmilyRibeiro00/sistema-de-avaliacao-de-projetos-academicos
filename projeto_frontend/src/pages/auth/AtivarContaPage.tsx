import { Alert, Button, Center, Paper, PasswordInput, Stack, Text, Title } from "@mantine/core";
import { IconInfoCircle } from "@tabler/icons-react";
import { useNavigate } from "react-router-dom";

export function AtivarContaPage() {
  const navigate = useNavigate();

  return (
    <Center mih="100vh" bg="gray.0">
      <Paper withBorder shadow="sm" radius="md" p="xl" w={420}>
        <Stack gap="md">
          <div>
            <Title order={3}>Ative sua conta</Title>
            <Text size="sm" c="dimmed">
              Sua conta foi criada a partir da lista de avaliadores da edição atual. Defina uma senha para
              começar a avaliar os projetos atribuídos a você.
            </Text>
          </div>

          <Alert icon={<IconInfoCircle size={16} />} color="blue" variant="light">
            Link de ativação válido por tempo limitado. Se expirou, peça ao administrador para reenviar.
          </Alert>

          <PasswordInput label="Nova senha" placeholder="mínimo 8 caracteres" />
          <PasswordInput label="Confirmar senha" placeholder="repita a senha" />

          <Button onClick={() => navigate("/login")}>Ativar conta</Button>
        </Stack>
      </Paper>
    </Center>
  );
}
