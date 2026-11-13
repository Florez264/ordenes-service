package com.subocol.taller.ordenes.application.usecase;

import com.subocol.taller.ordenes.application.dto.OrdenDetalle;
import com.subocol.taller.ordenes.domain.exception.RecursoNoEncontradoException;
import com.subocol.taller.ordenes.domain.repository.OrdenRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ConsultarOrdenUseCase {
  private final OrdenRepository repository;

  public OrdenDetalle ejecutar(UUID id) {
    return repository
        .buscarPorId(id)
        .map(OrdenDetalle::desde)
        .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
  }
}
