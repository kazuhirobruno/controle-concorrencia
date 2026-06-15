package br.com.example.kazuhiro.controletransaction.exceptions;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Void> handleReadableExceptions(HttpMessageNotReadableException ex) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build(); // Retorna 422
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build(); // Retorna 422
  }
}
