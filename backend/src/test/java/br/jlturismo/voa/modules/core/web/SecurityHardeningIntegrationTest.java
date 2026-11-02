package br.jlturismo.voa.modules.core.web;

import br.jlturismo.voa.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityHardeningIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @DynamicPropertySource
    static void rateLimits(DynamicPropertyRegistry registry) {
        registry.add("voa.public-api.post-rate-limit-per-minute", () -> "2");
        registry.add("voa.auth.login-rate-limit-per-minute", () -> "2");
    }

    @Test
    void headersSegurancaNaApi() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void rateLimitLogin() throws Exception {
        String body =
                """
                {"email":"admin@test.local","password":"AdminSenhaSegura12"}
                """;
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", "10.0.0.10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", "10.0.0.10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", "10.0.0.10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void rateLimitPublicoPostSolicitacao() throws Exception {
        String body =
                """
                {
                  "nome":"Maria",
                  "email":"maria@example.com",
                  "telefone":"11999999999",
                  "titulo":"Férias",
                  "destino":"Lisboa"
                }
                """;
        mockMvc.perform(post("/api/v1/public/solicitacoes-viagem")
                        .header("X-Forwarded-For", "10.0.0.20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/public/solicitacoes-viagem")
                        .header("X-Forwarded-For", "10.0.0.20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/public/solicitacoes-viagem")
                        .header("X-Forwarded-For", "10.0.0.20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void sanitizaHtmlEmSolicitacaoPublica() throws Exception {
        mockMvc.perform(post("/api/v1/public/solicitacoes-viagem")
                        .header("X-Forwarded-For", "10.0.0.30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "nome":"<b>João</b>",
                                  "email":"joao@example.com",
                                  "titulo":"Viagem",
                                  "destino":"Paris"
                                }
                                """))
                .andExpect(status().isOk());
    }
}
