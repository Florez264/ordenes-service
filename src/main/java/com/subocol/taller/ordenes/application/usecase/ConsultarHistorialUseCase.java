package com.subocol.taller.ordenes.application.usecase;

import com.subocol.taller.ordenes.application.dto.Pagina;
import com.subocol.taller.ordenes.application.port.HistorialOrdenPort;
import com.subocol.taller.ordenes.domain.exception.RecursoNoEncontradoException;
import com.subocol.taller.ordenes.domain.model.CambioEstado;
import com.subocol.taller.ordenes.domain.repository.OrdenRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ConsultarHistorialUseCase {
  private final OrdenRepository ordenes;
  private final HistorialOrdenPort historial;

  public Pagina<CambioEstado> ejecutar(UUID id, int page, int size) {
    if (page < 0 || size < 1 || size > 50 || (long) page * size > Integer.MAX_VALUE)
      throw new IllegalArgumentException("Paginación inválida");
    ordenes
        .buscarPorId(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
    return historial.listar(id, page, size);
  }
}
