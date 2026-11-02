package br.jlturismo.voa.modules.integracao;

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

/**
 * Fluxo ponta a ponta (spec §22): área pública → comercial → financeiro → parceiro/comissão manual → notificações.
 */
class VoaFullFlowIntegrationTest extends AbstractIntegrationTest {

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
    void fluxoCompletoPublicoComercialFinanceiroParceiroNotificacoes() throws Exception {
        mockMvc.perform(post("/api/v1/public/solicitacoes-viagem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Maria Publica",
                                  "email": "maria.publica@example.com",
                                  "telefone": "11999990000",
                                  "titulo": "Férias Nordeste",
                                  "destino": "Salvador",
                                  "dataIda": "%s"
                                }
                                """
                                .formatted(LocalDate.now().plusMonths(2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEBIDA"));

        mockMvc.perform(post("/api/v1/promocoes")
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Verão 2026",
                                  "descricao": "Pacotes selecionados",
                                  "destaque": true,
                                  "ativo": true
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/public/promocoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Verão 2026"));

        mockMvc.perform(get("/api/v1/notificacoes/nao-lidas/count").cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));

        String vendaId = criarVendaConfirmada();

        MvcResult parceiroResult = mockMvc.perform(post("/api/v1/parceiros")
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Hotel Parceiro","percentualReferencia":4.0}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String parceiroId =
                objectMapper.readTree(parceiroResult.getResponse().getContentAsString()).get("id").asText();

        MvcResult operacaoResult = mockMvc.perform(post("/api/v1/operacoes")
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendaId":"%s","parceiroId":"%s"}
                                """
                                .formatted(vendaId, parceiroId)))
                .andExpect(status().isOk())
                .andReturn();
        String operacaoId =
                objectMapper.readTree(operacaoResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/v1/operacoes/%s/comissao-linhas".formatted(operacaoId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"descricao":"Comissão manual","valor":120.00,"usarCalculoAutomatico":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(120.0));

        mockMvc.perform(post("/api/v1/operacoes/%s/comissao-linhas".formatted(operacaoId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"descricao":"Auto bloqueado","usarCalculoAutomatico":true}
                                """))
                .andExpect(status().isConflict());

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
                .andReturn();

        String pagamentoJson = pagamentoResult.getResponse().getContentAsString();
        String parcelaId = objectMapper.readTree(pagamentoJson).get("parcelas").get(0).get("id").asText();
        String pagamentoId = objectMapper.readTree(pagamentoJson).get("id").asText();

        mockMvc.perform(post("/api/v1/parcelas/%s/confirmar".formatted(parcelaId))
                        .with(csrf())
                        .cookie(accessCookie))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/pagamentos/%s/confirmar-quitacao".formatted(pagamentoId))
                        .with(csrf())
                        .cookie(accessCookie))
                .andExpect(status().isOk());

        MvcResult bilheteResult = mockMvc.perform(post("/api/v1/vendas/%s/bilhetes".formatted(vendaId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"AEREO","localizador":"E2E123"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String bilheteId =
                objectMapper.readTree(bilheteResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(patch("/api/v1/bilhetes/%s".formatted(bilheteId))
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"EMITIDA","arquivoPath":"/data/bilhetes/e2e.pdf"}
                                """))
                .andExpect(status().isOk());

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
                .andExpect(jsonPath("$.solicitacoesAtivas").isNumber());

        MvcResult notifPage = mockMvc.perform(get("/api/v1/notificacoes").cookie(accessCookie))
                .andExpect(status().isOk())
                .andReturn();
        String notifId = objectMapper
                .readTree(notifPage.getResponse().getContentAsString())
                .get("content")
                .get(0)
                .get("id")
                .asText();

        mockMvc.perform(post("/api/v1/notificacoes/%s/marcar-lida".formatted(notifId))
                        .with(csrf())
                        .cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lida").value(true));
    }

    private String criarVendaConfirmada() throws Exception {
        MvcResult clienteResult = mockMvc.perform(post("/api/v1/clientes")
                        .with(csrf())
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"E2E Ltda","email":"e2e@corp.com"}
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
                                  "titulo": "Viagem E2E",
                                  "destino": "Recife"
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
                                  "descricao": "GRU-REC",
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
                        .header("Idempotency-Key", "e2e-flow-" + cotacaoId))
                .andExpect(status().isOk())
                .andReturn();
        String vendaId =
                objectMapper.readTree(vendaResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/v1/vendas/%s/confirmar".formatted(vendaId))
                        .with(csrf())
                        .cookie(accessCookie))
                .andExpect(status().isOk());

        return vendaId;
    }
}
