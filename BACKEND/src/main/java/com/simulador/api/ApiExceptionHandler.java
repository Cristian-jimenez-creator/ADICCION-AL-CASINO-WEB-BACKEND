package com.simulador.api;

import com.mongodb.MongoException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Convierte excepciones de la API en respuestas HTTP. */
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String,String>> badRequest(IllegalArgumentException ex) { return response(HttpStatus.BAD_REQUEST,ex.getMessage()); }
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String,String>> conflict(IllegalStateException ex) { return response(HttpStatus.CONFLICT,ex.getMessage()); }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String,String>> forbidden(AccessDeniedException ex) { return response(HttpStatus.FORBIDDEN,ex.getMessage()); }
    @ExceptionHandler(DuplicateKeyException.class)
    ResponseEntity<Map<String,String>> duplicate(DuplicateKeyException ex) { return response(HttpStatus.CONFLICT,"Ya existe un registro con esos datos."); }
    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<Map<String,String>> database(DataAccessException ex) { return response(HttpStatus.SERVICE_UNAVAILABLE,"La base de datos está ocupada. Espera un momento y vuelve a intentarlo."); }
    @ExceptionHandler(MongoException.class)
    ResponseEntity<Map<String,String>> mongo(MongoException ex) { return response(HttpStatus.SERVICE_UNAVAILABLE,"La base de datos está ocupada. Espera un momento y vuelve a intentarlo."); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String,String>> invalid(MethodArgumentNotValidException ex) {
        String message=ex.getBindingResult().getFieldErrors().stream().findFirst().map(e->e.getDefaultMessage()).orElse("Revisa los datos enviados.");
        return response(HttpStatus.BAD_REQUEST,message);
    }
    private ResponseEntity<Map<String,String>> response(HttpStatus status,String message) {
        return ResponseEntity.status(status).body(Map.of("message",message==null?"Solicitud no válida.":message));
    }
}
