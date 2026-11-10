package com.relatosburger.api.cardapio;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
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
class CardapioControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void listaCategorias() throws Exception {
		mockMvc.perform(get("/api/cardapio/categorias"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[0].id").isNumber())
				.andExpect(jsonPath("$[0].nome").isString());
	}

	@Test
	void listaProdutosSemFiltro() throws Exception {
		mockMvc.perform(get("/api/cardapio/produtos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(10)));
	}

	@Test
	void listaProdutosComFiltroDeCategoria() throws Exception {
		mockMvc.perform(get("/api/cardapio/produtos").param("categoria", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(4)))
				.andExpect(jsonPath("$[*].categoriaId", everyItem(is(1))));
	}

	@Test
	void produtoInexistenteRetorna404Padronizado() throws Exception {
		mockMvc.perform(get("/api/cardapio/produtos/999999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.mensagem").isString())
				.andExpect(jsonPath("$.timestamp").exists());
	}

}
