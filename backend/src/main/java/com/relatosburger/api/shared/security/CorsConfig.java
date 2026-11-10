package com.relatosburger.api.shared.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${app.cors.origens-permitidas:http://localhost:3000}") String origensPermitidas) {
		CorsConfiguration configuracao = new CorsConfiguration();
		configuracao.setAllowedOrigins(Arrays.stream(origensPermitidas.split(","))
				.map(String::trim)
				.toList());
		configuracao.setAllowedMethods(List.of("GET"));
		configuracao.setAllowedHeaders(List.of("*"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuracao);
		return source;
	}

}
