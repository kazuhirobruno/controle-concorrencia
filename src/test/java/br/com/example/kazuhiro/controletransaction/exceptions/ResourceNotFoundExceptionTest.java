package br.com.example.kazuhiro.controletransaction.exceptions;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

  @Test
  @DisplayName("Deve carregar a mensagem de erro corretamente ao instanciar a exceção.")
  void shouldCarregarMensagemCorretamente() {
    String expectedMessage = "Recurso indisponível ou inexistente.";

    ResourceNotFoundException exception = new ResourceNotFoundException(expectedMessage);

    Assertions.assertThat(exception)
        .isInstanceOf(RuntimeException.class)
        .hasMessage(expectedMessage);
  }
}
