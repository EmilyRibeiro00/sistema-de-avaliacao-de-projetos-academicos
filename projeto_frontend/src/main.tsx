import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { MantineProvider, createTheme } from "@mantine/core";
import { Notifications } from "@mantine/notifications";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter } from "react-router-dom";

import "@mantine/core/styles.css";
import "@mantine/notifications/styles.css";
import "./index.css";

import App from "./App";

const theme = createTheme({
  primaryColor: "navy",
  colors: {
    navy: [
      "#eef1f8",
      "#d7ddec",
      "#adb9d6",
      "#8093c0",
      "#5b73ae",
      "#3f5aa1",
      "#2d4a97",
      "#1e3a82", // primary shade (index 7)
      "#152c66",
      "#0c1c47",
    ],
  },
  defaultRadius: "md",
  fontFamily: "Inter, system-ui, sans-serif",
  headings: { fontFamily: "Inter, system-ui, sans-serif", fontWeight: "700" },
});

const queryClient = new QueryClient();

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <MantineProvider theme={theme} defaultColorScheme="light">
      <Notifications position="top-right" />
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </QueryClientProvider>
    </MantineProvider>
  </StrictMode>
);
