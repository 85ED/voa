package br.jlturismo.voa.modules.solicitacoes;

import br.jlturismo.voa.AbstractIntegrationTest;
import br.jlturismo.voa.modules.auth.application.AuthCookies;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SolicitacaoFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private Cookie accessCookie;

    @BeforeEach
    void login() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.local","password":"AdminSenhaSegura12"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        for (Cookie cookie : login.getResponse().getCookies()) {
            if (AuthCookies.ACCESS.equals(cookie.getName())) {
                accessCookie = cookie;
                break;
            }
        }
    }

    @Test
    void fluxoClienteESolicitacao() throws Exception {
        MvcResult clienteResult = mockMvc.perform(post("/api/v1/clientes")
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Maria Silva","email":"maria@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andReturn();

        JsonNode cliente = objectMapper.readTree(clienteResult.getResponse().getContentAsString());
        String clienteId = cliente.get("id").asText();

        mockMvc.perform(post("/api/v1/solicitacoes")
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": "%s",
                                  "titulo": "Viagem corporativa SP",
                                  "destino": "São Paulo",
                                  "dataIda": "2026-12-01",
                                  "dataVolta": "2026-12-05"
                                }
                                """.formatted(clienteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andExpect(jsonPath("$.titulo").value("Viagem corporativa SP"));

        mockMvc.perform(get("/api/v1/solicitacoes")
                        .cookie(accessCookie)
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}
