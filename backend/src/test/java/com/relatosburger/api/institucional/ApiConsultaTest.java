package com.relatosburger.api.institucional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ApiConsultaTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void retornaInformacoesInstitucionais() throws Exception {
		mockMvc.perform(get("/api/institucional"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").isString())
				.andExpect(jsonPath("$.descricao").isString())
				.andExpect(jsonPath("$.endereco").isString())
				.andExpect(jsonPath("$.telefone").isString())
				.andExpect(jsonPath("$.horarioFuncionamento").isString());
	}

	@Test
	void retornaStatus() throws Exception {
		mockMvc.perform(get("/api/status"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("ok")));
	}

}
