package com.subocol.taller.ordenes.application.dto;

import com.subocol.taller.ordenes.domain.model.*;
import java.time.Instant;
import java.util.UUID;

public record OrdenDetalle(
    UUID id,
    Cliente cliente,
    Vehiculo vehiculo,
    Aseguradora aseguradora,
    String descripcion,
    EstadoOrden estado,
    Instant fechaCreacion,
    Long version) {
  public static OrdenDetalle desde(OrdenServicio o) {
    return new OrdenDetalle(
        o.getId(),
        o.getCliente(),
        o.getVehiculo(),
        o.getAseguradora(),
        o.getDescripcion(),
        o.getEstado(),
        o.getFechaCreacion(),
        o.getVersion());
  }
}
