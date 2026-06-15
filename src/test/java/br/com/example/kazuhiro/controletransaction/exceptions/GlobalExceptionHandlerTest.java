package br.com.example.kazuhiro.controletransaction.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler globalExceptionHandler;

  @BeforeEach
  void setUp() {
    globalExceptionHandler = new GlobalExceptionHandler();
  }

  @Test
  @DisplayName("Deve retornar a mensagem do erro de validação específico do campo com status 422")
  void shouldReturnFieldDefaultMessageWithStatus422() {
    MethodArgumentNotValidException exceptionMock = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResultMock = mock(BindingResult.class);
    FieldError fieldErrorMock = mock(FieldError.class);

    when(exceptionMock.getBindingResult()).thenReturn(bindingResultMock);
    when(bindingResultMock.getFieldError()).thenReturn(fieldErrorMock);
    when(fieldErrorMock.getDefaultMessage()).thenReturn("A descrição deve conter entre 1 e 10 caracteres.");

    ResponseEntity<String> response = globalExceptionHandler.handleValidationExceptions(exceptionMock);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody()).isEqualTo("A descrição deve conter entre 1 e 10 caracteres.");
  }

  @Test
  @DisplayName("Deve retornar mensagem genérica com status 422 quando o erro de validação for global ou nulo")
  void shouldReturnFallbackMessageWithStatus422WhenFieldErrorIsNull() {
    MethodArgumentNotValidException exceptionMock = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResultMock = mock(BindingResult.class);

    when(exceptionMock.getBindingResult()).thenReturn(bindingResultMock);
    when(bindingResultMock.getFieldError()).thenReturn(null);

    ResponseEntity<String> response = globalExceptionHandler.handleValidationExceptions(exceptionMock);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody()).isEqualTo("Erro de validação nos dados enviados.");
  }

  @Test
  @DisplayName("Deve retornar status 422 sem corpo quando a requisição não puder ser lida (HttpMessageNotReadableException)")
  void shouldReturnStatus422WithNoContentWhenHttpMessageNotReadable() {
    HttpMessageNotReadableException exceptionMock = mock(HttpMessageNotReadableException.class);

    ResponseEntity<Void> response = globalExceptionHandler.handleReadableExceptions(exceptionMock);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody()).isNull();
  }

  @Test
  @DisplayName("Deve retornar status 422 sem corpo quando houver incompatibilidade de tipo no argumento (MethodArgumentTypeMismatchException)")
  void shouldReturnStatus422WithNoContentWhenMethodArgumentTypeMismatch() {
    MethodArgumentTypeMismatchException exceptionMock = mock(MethodArgumentTypeMismatchException.class);

    ResponseEntity<Void> response = globalExceptionHandler.handleTypeMismatch(exceptionMock);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody()).isNull();
  }
}
