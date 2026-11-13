package com.subocol.taller.ordenes;

import static org.assertj.core.api.Assertions.*;

import com.subocol.taller.ordenes.application.dto.ListarOrdenes;
import com.subocol.taller.ordenes.domain.model.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class OrdenDominioTest {
  @Test
  void registraConEstadoInicialYNormalizaValores() {
    var orden =
        OrdenServicio.registrar(
            new Cliente(" Ana ", "123", "+573001234567"),
            new Vehiculo("abc123", "Toyota", "Corolla"),
            null,
            " Revisar frenos ",
            Instant.EPOCH);
    assertThat(orden.getId()).isNotNull();
    assertThat(orden.getEstado()).isEqualTo(EstadoOrden.REGISTRADA);
    assertThat(orden.getCliente().nombre()).isEqualTo("Ana");
    assertThat(orden.getVehiculo().placa()).isEqualTo("ABC123");
    assertThat(orden.getDescripcion()).isEqualTo("Revisar frenos");
    assertThat(orden.getVersion()).isNull();
  }

  @Test
  void protegeInvariantesSinDependerDeValidacionHttp() {
    assertThatThrownBy(() -> new Cliente(" ", "123", "3001234567"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Cliente("Ana", "123", "incorrecto"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Vehiculo("!", "Toyota", "Corolla"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void limitaLaPaginaYNormalizaFiltros() {
    assertThatThrownBy(() -> new ListarOrdenes(0, 51, null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ListarOrdenes(-1, 20, null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(new ListarOrdenes(0, 20, null, "abc123").placa()).isEqualTo("ABC123");
  }
}
