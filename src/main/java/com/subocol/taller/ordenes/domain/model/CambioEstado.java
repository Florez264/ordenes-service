package com.subocol.taller.ordenes.domain.model;

import java.time.Instant;
import java.util.UUID;

public record CambioEstado(
    UUID id,
    UUID ordenId,
    EstadoOrden anterior,
    EstadoOrden nuevo,
    String motivo,
    String actor,
    Instant fecha,
    long secuencia) {
  public CambioEstado {
    java.util.Objects.requireNonNull(id);
    java.util.Objects.requireNonNull(ordenId);
    java.util.Objects.requireNonNull(nuevo);
    java.util.Objects.requireNonNull(fecha);
    motivo = Texto.requerido(motivo, 500, "Motivo");
    actor = Texto.requerido(actor, 100, "Actor");
    if (secuencia < 0) throw new IllegalArgumentException("Secuencia inválida");
  }
}
