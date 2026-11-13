package com.subocol.taller.ordenes;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.subocol.taller.ordenes.application.dto.*;
import com.subocol.taller.ordenes.application.port.*;
import com.subocol.taller.ordenes.application.usecase.*;
import com.subocol.taller.ordenes.domain.exception.*;
import com.subocol.taller.ordenes.domain.model.*;
import com.subocol.taller.ordenes.domain.repository.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RegistrarOrdenUseCaseTest {
  private final OrdenRepository repository = mock(OrdenRepository.class);
  private final PoliticaRecepcionOrdenPort politica = mock(PoliticaRecepcionOrdenPort.class);
  private final HistorialOrdenPort historial = mock(HistorialOrdenPort.class);
  private final UnidadTrabajoPort unidad =
      new UnidadTrabajoPort() {
        public <T> T ejecutar(java.util.function.Supplier<T> operacion) {
          return operacion.get();
        }
      };
  private final Clock clock = Clock.fixed(Instant.parse("2026-11-12T12:00:00Z"), ZoneOffset.UTC);

  @Test
  void rechazaAseguradoraIntegradaSinGuardarNada() {
    when(politica.admiteRegistroManual("INTEGRADA_DEMO")).thenReturn(false);
    var useCase =
        new RegistrarOrdenUseCase(repository, politica, clock, historial, () -> "asesor", unidad);
    var command =
        new RegistrarOrden(
            new Cliente("Ana", "123", "3001234567"),
            new Vehiculo("ABC123", "Toyota", "Corolla"),
            new Aseguradora("integrada_demo", "Demo"),
            "Frenos");
    assertThatThrownBy(() -> useCase.ejecutar(command)).isInstanceOf(ReglaNegocioException.class);
    verifyNoInteractions(repository);
  }

  @Test
  void registraParticularConElRelojInyectado() {
    when(repository.guardar(any()))
        .thenAnswer(
            invocation -> {
              OrdenServicio o = invocation.getArgument(0);
              return OrdenServicio.reconstituir(
                  o.getId(),
                  o.getCliente(),
                  o.getVehiculo(),
                  o.getAseguradora(),
                  o.getDescripcion(),
                  o.getEstado(),
                  o.getFechaCreacion(),
                  0L);
            });
    var useCase =
        new RegistrarOrdenUseCase(repository, politica, clock, historial, () -> "asesor", unidad);
    var result =
        useCase.ejecutar(
            new RegistrarOrden(
                new Cliente("Ana", "123", "3001234567"),
                new Vehiculo("ABC123", "Toyota", "Corolla"),
                null,
                "Frenos"));
    var captor = ArgumentCaptor.forClass(OrdenServicio.class);
    verify(repository).guardar(captor.capture());
    assertThat(captor.getValue().getFechaCreacion()).isEqualTo(clock.instant());
    assertThat(result.version()).isZero();
    verifyNoInteractions(politica);
  }
}
