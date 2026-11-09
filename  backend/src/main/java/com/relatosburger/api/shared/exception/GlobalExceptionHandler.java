package com.relatosburger.api.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> tratarApi(ApiException ex, HttpServletRequest req) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiError.of(ex.getStatus(), ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> tratarValidacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiError.CampoErro> campos = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(f -> campos.add(new ApiError.CampoErro(f.getField(), f.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors()
                .forEach(g -> campos.add(new ApiError.CampoErro(g.getObjectName(), g.getDefaultMessage())));

        ApiError corpo = new ApiError(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Dados inválidos", req.getRequestURI(), campos);
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> tratarCorpoIlegivel(HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido", req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> tratarGenerico(Exception ex, HttpServletRequest req) {
        // Erros padrão do Spring MVC (404, 405, 415...) mantêm o status original
        if (ex instanceof ErrorResponse er) {
            HttpStatus status = HttpStatus.valueOf(er.getStatusCode().value());
            return ResponseEntity.status(status)
                    .body(ApiError.of(status, status.getReasonPhrase(), req.getRequestURI()));
        }
        log.error("Erro inesperado em {}", req.getRequestURI(), ex);
        return ResponseEntity.internalServerError()
                .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor", req.getRequestURI()));
    }
}