package com.relatosburger.api.shared.exception;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException excecao) {
		ErroResponse erro = new ErroResponse(HttpStatus.NOT_FOUND.value(), excecao.getMessage(), Instant.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroResponse> tratarErroGenerico(Exception excecao) {
		LOGGER.error("Erro interno ao processar requisicao ({})", excecao.getClass().getSimpleName());
		ErroResponse erro = new ErroResponse(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Erro interno do servidor",
				Instant.now());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
	}

}
