package br.jlturismo.voa.modules.financeiro;

import br.jlturismo.voa.AbstractIntegrationTest;
import br.jlturismo.voa.modules.auth.application.AuthCookies;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FinanceiroFlowIntegrationTest extends AbstractIntegrationTest {

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
    void pagamentoConfirmadoDisparaEmissao() throws Exception {
        String vendaId = criarVendaConfirmada();

        MvcResult pagamentoResult = mockMvc.perform(post("/api/v1/vendas/%s/pagamentos".formatted(vendaId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "forma": "PIX",
                                  "parcelas": [
                                    {"vencimento": "%s", "valor": 1500.00}
                                  ]
                                }
                                """
                                .formatted(LocalDate.now().plusDays(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn();

        String pagamentoJson = pagamentoResult.getResponse().getContentAsString();
        String parcelaId = objectMapper.readTree(pagamentoJson).get("parcelas").get(0).get("id").asText();
        String pagamentoId = objectMapper.readTree(pagamentoJson).get("id").asText();

        mockMvc.perform(patch("/api/v1/parcelas/%s".formatted(parcelaId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comprovanteRef": "s3://placeholder/comprovante.pdf"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/parcelas/%s/confirmar".formatted(parcelaId))
                        .with(csrf())
                        .cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARCIAL"));

        mockMvc.perform(get("/api/v1/vendas/%s".formatted(vendaId)).cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGAMENTO_PARCIAL"));

        mockMvc.perform(post("/api/v1/pagamentos/%s/confirmar-quitacao".formatted(pagamentoId))
                        .with(csrf())
                        .cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUITADO"));

        mockMvc.perform(get("/api/v1/vendas/%s".formatted(vendaId)).cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_EMISSAO"));

        MvcResult bilheteResult = mockMvc.perform(post("/api/v1/vendas/%s/bilhetes".formatted(vendaId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"AEREO","localizador":"ABC123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn();

        String bilheteId =
                objectMapper.readTree(bilheteResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(patch("/api/v1/bilhetes/%s".formatted(bilheteId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"EMITIDA","arquivoPath":"/data/bilhetes/abc123.pdf"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EMITIDA"));

        mockMvc.perform(get("/api/v1/vendas/%s".formatted(vendaId)).cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_EMISSAO"));

        mockMvc.perform(patch("/api/v1/bilhetes/%s".formatted(bilheteId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ENVIADA_CLIENTE"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/vendas/%s".formatted(vendaId)).cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));

        mockMvc.perform(get("/api/v1/dashboard/resumo").cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendasAguardandoPagamento").isNumber());

        mockMvc.perform(get("/api/v1/audit/eventos").cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    private String criarVendaConfirmada() throws Exception {
        MvcResult clienteResult = mockMvc.perform(post("/api/v1/clientes")
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Pagamento Ltda","email":"fin@corp.com"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String clienteId = objectMapper.readTree(clienteResult.getResponse().getContentAsString()).get("id").asText();

        MvcResult solicitacaoResult = mockMvc.perform(post("/api/v1/solicitacoes")
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": "%s",
                                  "titulo": "Viagem SP",
                                  "destino": "São Paulo"
                                }
                                """.formatted(clienteId)))
                .andExpect(status().isOk())
                .andReturn();
        String solicitacaoId =
                objectMapper.readTree(solicitacaoResult.getResponse().getContentAsString()).get("id").asText();

        MvcResult cotacaoResult = mockMvc.perform(post("/api/v1/solicitacoes/%s/cotacoes".formatted(solicitacaoId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"validade":"%s"}
                                """.formatted(LocalDate.now().plusDays(5))))
                .andExpect(status().isOk())
                .andReturn();
        String cotacaoId =
                objectMapper.readTree(cotacaoResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/v1/cotacoes/%s/itens".formatted(cotacaoId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "AEREO",
                                  "descricao": "GRU-CGH",
                                  "quantidade": 1,
                                  "valorUnitario": 1500.00
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/cotacoes/%s/enviar".formatted(cotacaoId))
                        .with(csrf())
                        .cookie(accessCookie))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/cotacoes/%s/aceitar".formatted(cotacaoId))
                        .with(csrf())
                        .cookie(accessCookie))
                .andExpect(status().isOk());

        MvcResult vendaResult = mockMvc.perform(post("/api/v1/cotacoes/%s/converter-venda".formatted(cotacaoId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .header("Idempotency-Key", "fin-flow-" + cotacaoId))
                .andExpect(status().isOk())
                .andReturn();
        String vendaId =
                objectMapper.readTree(vendaResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/v1/vendas/%s/confirmar".formatted(vendaId))
                        .with(csrf())
                        .cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));

        return vendaId;
    }
}
