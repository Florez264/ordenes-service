package com.subocol.taller.ordenes.application.usecase;

import com.subocol.taller.ordenes.application.dto.*;
import com.subocol.taller.ordenes.application.port.*;
import com.subocol.taller.ordenes.domain.exception.ReglaNegocioException;
import com.subocol.taller.ordenes.domain.model.CambioEstado;
import com.subocol.taller.ordenes.domain.model.OrdenServicio;
import com.subocol.taller.ordenes.domain.repository.OrdenRepository;
import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RegistrarOrdenUseCase {
  private final OrdenRepository repository;
  private final PoliticaRecepcionOrdenPort politica;
  private final Clock clock;
  private final HistorialOrdenPort historial;
  private final ActorActualPort actor;
  private final UnidadTrabajoPort unidad;

  public OrdenDetalle ejecutar(RegistrarOrden command) {
    if (command.aseguradora() != null
        && !politica.admiteRegistroManual(command.aseguradora().codigo()))
      throw new ReglaNegocioException("Esta aseguradora solo admite órdenes por integración");
    var orden =
        OrdenServicio.registrar(
            command.cliente(),
            command.vehiculo(),
            command.aseguradora(),
            command.descripcion(),
            clock.instant());
    return unidad.ejecutar(
        () -> {
          var guardada = repository.guardar(orden);
          historial.agregar(
              new CambioEstado(
                  UUID.randomUUID(),
                  guardada.getId(),
                  null,
                  guardada.getEstado(),
                  "Registro de orden",
                  actor.identificador(),
                  clock.instant(),
                  guardada.getVersion()));
          return OrdenDetalle.desde(guardada);
        });
  }
}
