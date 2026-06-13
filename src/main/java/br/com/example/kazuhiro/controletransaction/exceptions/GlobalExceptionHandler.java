package br.com.example.kazuhiro.controletransaction.exceptions;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        var fieldError = ex.getBindingResult().getFieldError();

        String message = (fieldError != null)
                ? fieldError.getDefaultMessage()
                : "Erro de validação nos dados enviados.";

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(message);
    }
}