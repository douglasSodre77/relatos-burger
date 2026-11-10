package com.relatosburger.api.shared.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("tls")
@TestPropertySource(properties = {
		"server.ssl.enabled=false",
		"server.ssl.key-store=classpath:nao-usado.p12",
		"server.ssl.key-store-password=nao-usado"
})
class TlsSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void permiteGetSobreHttps() throws Exception {
		mockMvc.perform(https(get("/api/cardapio/produtos")))
				.andExpect(status().isOk());
	}

	@Test
	void rejeitaPostNoProfileTls() throws Exception {
		mockMvc.perform(https(post("/api/cardapio/produtos")))
				.andExpect(status().isForbidden());
	}

	@Test
	void rejeitaDeleteNoProfileTls() throws Exception {
		mockMvc.perform(https(delete("/api/cardapio/produtos/1")))
				.andExpect(status().isForbidden());
	}

	private static MockHttpServletRequestBuilder https(MockHttpServletRequestBuilder builder) {
		return builder.secure(true).with(request -> {
			request.setScheme("https");
			return request;
		});
	}

}
