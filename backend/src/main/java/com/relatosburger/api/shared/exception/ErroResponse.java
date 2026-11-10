package com.relatosburger.api.shared.exception;

import java.time.Instant;

public record ErroResponse(int status, String mensagem, Instant timestamp) {
}
