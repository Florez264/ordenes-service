package com.subocol.taller.ordenes.application.usecase;

import com.subocol.taller.ordenes.application.dto.OrdenDetalle;
import com.subocol.taller.ordenes.application.port.*;
import com.subocol.taller.ordenes.domain.exception.RecursoNoEncontradoException;
import com.subocol.taller.ordenes.domain.model.*;
import com.subocol.taller.ordenes.domain.repository.OrdenRepository;
import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class CambiarEstadoUseCase {
  private final OrdenRepository repository;
  private final HistorialOrdenPort historial;
  private final ActorActualPort actor;
  private final UnidadTrabajoPort unidad;
  private final Clock clock;
  private final CierresOrdenPort cierres;

  public OrdenDetalle ejecutar(
      UUID id, EstadoOrden destino, String motivo, long version, boolean reapertura) {
    return unidad.ejecutar(
        () -> {
          var anterior =
              repository
                  .buscarPorId(id)
                  .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
          var modificada =
              reapertura
                  ? anterior.reabrir(motivo, version)
                  : anterior.cambiarEstado(destino, motivo, version);
          var guardada = repository.guardar(modificada);
          historial.agregar(
              new CambioEstado(
                  UUID.randomUUID(),
                  id,
                  anterior.getEstado(),
                  guardada.getEstado(),
                  motivo,
                  actor.identificador(),
                  clock.instant(),
                  guardada.getVersion()));
          if (guardada.getEstado() == EstadoOrden.CERRADA)
            cierres.registrar(id, guardada.getVersion());
          return OrdenDetalle.desde(guardada);
        });
  }
}
