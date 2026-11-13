package com.subocol.taller.ordenes.domain.model;

import java.time.Instant;
import java.util.*;
import lombok.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrdenServicio {
  private final UUID id;
  private final Cliente cliente;
  private final Vehiculo vehiculo;
  private final Aseguradora aseguradora;
  private final String descripcion;
  private final EstadoOrden estado;
  private final Instant fechaCreacion;
  private final Long version;

  public static OrdenServicio registrar(
      Cliente cliente,
      Vehiculo vehiculo,
      Aseguradora aseguradora,
      String descripcion,
      Instant fecha) {
    return new OrdenServicio(
        UUID.randomUUID(),
        Objects.requireNonNull(cliente),
        Objects.requireNonNull(vehiculo),
        aseguradora,
        Texto.requerido(descripcion, 2000, "Descripción"),
        EstadoOrden.REGISTRADA,
        Objects.requireNonNull(fecha),
        null);
  }

  public static OrdenServicio reconstituir(
      UUID id,
      Cliente cliente,
      Vehiculo vehiculo,
      Aseguradora aseguradora,
      String descripcion,
      EstadoOrden estado,
      Instant fecha,
      Long version) {
    return new OrdenServicio(
        Objects.requireNonNull(id),
        Objects.requireNonNull(cliente),
        Objects.requireNonNull(vehiculo),
        aseguradora,
        Texto.requerido(descripcion, 2000, "Descripción"),
        Objects.requireNonNull(estado),
        Objects.requireNonNull(fecha),
        Objects.requireNonNull(version));
  }

  public OrdenServicio cambiarEstado(EstadoOrden destino, String motivo, long versionEsperada) {
    validarCambio(motivo, versionEsperada);
    if (destino == null || !estado.permite(destino))
      throw new com.subocol.taller.ordenes.domain.exception.ReglaNegocioException(
          "Transición de estado no permitida");
    return conEstado(destino);
  }

  public OrdenServicio reabrir(String motivo, long versionEsperada) {
    validarCambio(motivo, versionEsperada);
    if (estado != EstadoOrden.CERRADA)
      throw new com.subocol.taller.ordenes.domain.exception.ReglaNegocioException(
          "Solo se puede reabrir una orden cerrada");
    return conEstado(EstadoOrden.EN_DIAGNOSTICO);
  }

  private void validarCambio(String motivo, long esperada) {
    Texto.requerido(motivo, 500, "Motivo");
    if (version == null || version.longValue() != esperada)
      throw new com.subocol.taller.ordenes.domain.exception.ReglaNegocioException(
          "La orden cambió; consulte su versión actual");
  }

  private OrdenServicio conEstado(EstadoOrden destino) {
    return new OrdenServicio(
        id, cliente, vehiculo, aseguradora, descripcion, destino, fechaCreacion, version);
  }
}
