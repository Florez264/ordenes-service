package com.subocol.taller.ordenes;

import static org.assertj.core.api.Assertions.*;

import com.subocol.taller.ordenes.infrastructure.web.response.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ContratoRespuestaTest {
  @Test
  void estadoHttpCoincideConElCuerpo() {
    var response = ApiResponses.created("dato");
    assertThat(response.getStatusCode().value())
        .isEqualTo(response.getBody().status())
        .isEqualTo(201);
  }

  @Test
  void impideDobleEnvolturaYEstadosSinCuerpo() {
    assertThatThrownBy(() -> ApiResponses.ok(new ApiResponse<>(200, "x")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ApiResponses.of(HttpStatus.NO_CONTENT, "x"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
