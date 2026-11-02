package br.jlturismo.voa.modules.integracao;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PartnerPortalIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private Cookie adminCookie;

    @BeforeEach
    void loginAdmin() throws Exception {
        adminCookie = loginAs("admin@test.local", "AdminSenhaSegura12");
    }

    @Test
    void portalParceiroSlugSolicitacaoEscopoPartner() throws Exception {
        MvcResult parceiroA = mockMvc.perform(post("/api/v1/parceiros")
                        .with(csrf())
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Parceiro A","slug":"parceiro-a","ativo":true}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String parceiroAId = objectMapper.readTree(parceiroA.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/public/parceiros/parceiro-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeExibicao").value("Parceiro A"));

        MvcResult publicReq = mockMvc.perform(post("/api/v1/public/parceiros/parceiro-a/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoViagem":"SOMENTE_IDA",
                                  "origem":"GRU",
                                  "destino":"MIA",
                                  "dataIda":"%s",
                                  "qtdPassageiros":2,
                                  "nome":"Cliente Partner",
                                  "email":"cliente@example.com",
                                  "telefone":"11988887777",
                                  "consentimentoLgpd":true,
                                  "website":""
                                }
                                """
                                .formatted(LocalDate.now().plusMonths(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andReturn();

        String solicitacaoId = objectMapper
                .readTree(publicReq.getResponse().getContentAsString())
                .get("solicitacaoId")
                .asText();

        mockMvc.perform(get("/api/v1/solicitacoes")
                        .cookie(adminCookie)
                        .param("parceiroId", parceiroAId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].parceiroId").value(parceiroAId));

        MvcResult acesso = mockMvc.perform(post("/api/v1/parceiros/%s/acesso".formatted(parceiroAId))
                        .with(csrf())
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"partner.a@test.local","nome":"Usuário A"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode acessoJson = objectMapper.readTree(acesso.getResponse().getContentAsString());
        String senhaTemp = acessoJson.get("senhaTemporaria").asText();

        Cookie partnerCookie = loginAs("partner.a@test.local", senhaTemp);

        mockMvc.perform(get("/api/v1/solicitacoes/" + solicitacaoId).cookie(partnerCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canal").value("PARCEIRO_LINK"));

        MvcResult parceiroB = mockMvc.perform(post("/api/v1/parceiros")
                        .with(csrf())
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Parceiro B","slug":"parceiro-b","ativo":true}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String parceiroBId =
                objectMapper.readTree(parceiroB.getResponse().getContentAsString()).get("id").asText();

        MvcResult publicB = mockMvc.perform(post("/api/v1/public/parceiros/parceiro-b/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoViagem":"SOMENTE_IDA",
                                  "origem":"CGH",
                                  "destino":"REC",
                                  "dataIda":"%s",
                                  "qtdPassageiros":1,
                                  "nome":"Outro Cliente",
                                  "email":"outro@example.com",
                                  "telefone":"11977776666",
                                  "consentimentoLgpd":true
                                }
                                """
                                .formatted(LocalDate.now().plusMonths(2))))
                .andExpect(status().isOk())
                .andReturn();
        String solicitacaoBId = objectMapper
                .readTree(publicB.getResponse().getContentAsString())
                .get("solicitacaoId")
                .asText();

        mockMvc.perform(get("/api/v1/solicitacoes/" + solicitacaoBId).cookie(partnerCookie))
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                        "/api/v1/parceiros/%s/slug".formatted(parceiroAId))
                                .with(csrf())
                                .cookie(adminCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"slug":"parceiro-a-novo"}
                                        """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/solicitacoes/" + solicitacaoId).cookie(partnerCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parceiroSlugSnapshot").value("parceiro-a"));
    }

    private Cookie loginAs(String email, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """
                                .formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        for (Cookie cookie : login.getResponse().getCookies()) {
            if (AuthCookies.ACCESS.equals(cookie.getName())) {
                return cookie;
            }
        }
        throw new IllegalStateException("Sem cookie de acesso");
    }
}
