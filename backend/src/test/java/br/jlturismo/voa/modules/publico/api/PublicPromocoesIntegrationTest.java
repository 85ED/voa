package br.jlturismo.voa.modules.publico.api;

import br.jlturismo.voa.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicPromocoesIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void listagemPublicaRetornaArrayJson() throws Exception {
        mockMvc.perform(get("/api/v1/public/promocoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
