package br.jlturismo.voa.modules.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.jlturismo.voa.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void loginSemCredenciaisRetorna401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginCredenciaisInvalidasRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"wrong@test.local","password":"wrongpassword12"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginAdminSeedFunciona() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.local","password":"AdminSenhaSegura12"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("admin@test.local"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void meSemAuthRetorna401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void z_redefinicaoSenhaComDevLink() throws Exception {
        var forgot = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.local"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.devResetUrl").isString())
                .andReturn();

        JsonNode body = objectMapper.readTree(forgot.getResponse().getContentAsString());
        String devUrl = body.get("devResetUrl").asText();
        String tokenParam = devUrl.substring(devUrl.indexOf("token=") + "token=".length());
        String token = URLDecoder.decode(tokenParam, StandardCharsets.UTF_8);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("token", token, "newPassword", "NovaSenhaSegura99"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.local","password":"NovaSenhaSegura99"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.local","password":"AdminSenhaSegura12"}
                                """))
                .andExpect(status().isBadRequest());

        restaurarSenhaAdminSeed();
    }

    /** Evita que outros testes de integração que usam AdminSenhaSegura12 falhem na mesma JVM. */
    private void restaurarSenhaAdminSeed() throws Exception {
        var forgot = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.local"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(forgot.getResponse().getContentAsString());
        String devUrl = body.get("devResetUrl").asText();
        String tokenParam = devUrl.substring(devUrl.indexOf("token=") + "token=".length());
        String token = URLDecoder.decode(tokenParam, StandardCharsets.UTF_8);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("token", token, "newPassword", "AdminSenhaSegura12"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.local","password":"AdminSenhaSegura12"}
                                """))
                .andExpect(status().isOk());
    }
}
