package com.Monarca.Backend.exception;

/**
 * Se lanza cuando se busca un registro (producto, categoría, etc.) que no existe.
 * {@link ManejadorGlobalErrores} la convierte en una respuesta HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
