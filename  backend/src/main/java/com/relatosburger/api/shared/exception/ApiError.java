package com.relatosburger.api.shared.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String erro,
        String caminho,
        List<CampoErro> campos
) {
    public record CampoErro(String campo, String mensagem) {}

    public static ApiError of(HttpStatus status, String erro, String caminho) {
        return new ApiError(LocalDateTime.now(), status.value(), erro, caminho, List.of());
    }
}