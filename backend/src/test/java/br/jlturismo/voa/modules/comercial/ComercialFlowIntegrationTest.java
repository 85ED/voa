package br.jlturismo.voa.modules.comercial;

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

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ComercialFlowIntegrationTest extends AbstractIntegrationTest {

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
    void fluxoCotacaoVendaPassageiro() throws Exception {
        MvcResult clienteResult = mockMvc.perform(post("/api/v1/clientes")
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"João Corp","email":"joao@corp.com"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String clienteId = objectMapper.readTree(clienteResult.getResponse().getContentAsString()).get("id").asText();

        MvcResult passageiroResult = mockMvc.perform(post("/api/v1/passageiros")
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":"%s","nome":"João Viajante","documento":"12345678900"}
                                """.formatted(clienteId)))
                .andExpect(status().isOk())
                .andReturn();
        String passageiroId =
                objectMapper.readTree(passageiroResult.getResponse().getContentAsString()).get("id").asText();

        MvcResult solicitacaoResult = mockMvc.perform(post("/api/v1/solicitacoes")
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": "%s",
                                  "titulo": "Evento RJ",
                                  "destino": "Rio de Janeiro"
                                }
                                """.formatted(clienteId)))
                .andExpect(status().isOk())
                .andReturn();
        String solicitacaoId =
                objectMapper.readTree(solicitacaoResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/v1/solicitacoes/%s/passageiros/%s".formatted(solicitacaoId, passageiroId))
                        .with(csrf())
                        .cookie(accessCookie))
                .andExpect(status().isOk());

        LocalDate validade = LocalDate.now().plusDays(7);
        MvcResult cotacaoResult = mockMvc.perform(post("/api/v1/solicitacoes/%s/cotacoes".formatted(solicitacaoId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"validade":"%s","observacoes":"Opções LATAM/GOL"}
                                """.formatted(validade)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andReturn();
        String cotacaoId =
                objectMapper.readTree(cotacaoResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/v1/cotacoes/%s/alternativas".formatted(cotacaoId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rotulo": "Opção A",
                                  "resumo": "GRU-GIG direto",
                                  "origem": "GRU",
                                  "destino": "GIG",
                                  "valorTotal": 1500.00
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/cotacoes/%s/itens".formatted(cotacaoId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "SERVICO",
                                  "descricao": "Taxa de serviço",
                                  "valorUnitario": 120.00
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/cotacoes/%s/enviar".formatted(cotacaoId))
                        .with(csrf())
                        .cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENVIADA"));

        mockMvc.perform(post("/api/v1/cotacoes/%s/aceitar".formatted(cotacaoId))
                        .with(csrf())
                        .cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACEITA"));

        mockMvc.perform(post("/api/v1/cotacoes/%s/converter-venda".formatted(cotacaoId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .header("Idempotency-Key", "test-key-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.total").value(120.0));

        mockMvc.perform(post("/api/v1/cotacoes/%s/converter-venda".formatted(cotacaoId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .header("Idempotency-Key", "test-key-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ABERTA"));

        mockMvc.perform(post("/api/v1/cotacoes/%s/enviar".formatted(cotacaoId))
                        .with(csrf())
                        .cookie(accessCookie))
                .andExpect(status().isConflict());
    }

    @Test
    void vendaManualEConfirmacao() throws Exception {
        MvcResult clienteResult = mockMvc.perform(post("/api/v1/clientes")
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","email":"ana@test.com"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String clienteId = objectMapper.readTree(clienteResult.getResponse().getContentAsString()).get("id").asText();

        MvcResult vendaResult = mockMvc.perform(post("/api/v1/vendas/manual")
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": "%s",
                                  "itens": [
                                    {"tipo":"AEREO","descricao":"SP-RJ","valorUnitario":800}
                                  ]
                                }
                                """.formatted(clienteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manual").value(true))
                .andReturn();
        String vendaId = objectMapper.readTree(vendaResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/v1/vendas/%s/confirmar".formatted(vendaId))
                        .with(csrf())
                        .cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));

        mockMvc.perform(post("/api/v1/vendas/%s/confirmar".formatted(vendaId))
                        .with(csrf())
                        .cookie(accessCookie))
                .andExpect(status().isConflict());
    }
}
