package com.Monarca.Backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Traduce las excepciones de la API a respuestas JSON con un código HTTP adecuado
 * y el formato {@code {"error": "mensaje"}} que ya consume el frontend.
 *
 * Sin este manejador, los errores de validación llegaban al cliente como HTTP 500.
 * Los controladores que capturan sus propias excepciones siguen funcionando igual,
 * porque un try/catch local tiene prioridad sobre este manejador.
 *
 * No captura {@code RuntimeException} ni {@code Exception} genéricas a propósito:
 * así Spring Security sigue respondiendo 401/403 y los errores inesperados no
 * exponen detalles internos.
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    /** Datos enviados por el cliente que no cumplen las reglas de negocio. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(RecursoNoEncontradoException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Errores que ya indican su propio código HTTP (p. ej. 409 en estados de pedido). */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> conEstado(ResponseStatusException ex) {
        return error(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
    }

    /** JSON mal formado o con tipos incorrectos (p. ej. texto donde se espera un número). */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> solicitudIlegible(Exception ex) {
        return error(HttpStatus.BAD_REQUEST, "La solicitud tiene un formato inválido.");
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus estado, String mensaje) {
        String texto = (mensaje == null || mensaje.isBlank()) ? estado.getReasonPhrase() : mensaje;
        return ResponseEntity.status(estado).body(Map.of("error", texto));
    }
}
