package com.subocol.taller.ordenes.infrastructure.persistence.entity;

import com.subocol.taller.ordenes.domain.model.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "orden_servicio")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrdenJpaEntity {
  @Id
  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(length = 36, nullable = false, updatable = false)
  private UUID id;

  @Column(name = "cliente_nombre", length = 150, nullable = false)
  private String clienteNombre;

  @Column(name = "cliente_documento", length = 30, nullable = false)
  private String clienteDocumento;

  @Column(name = "cliente_telefono", length = 25, nullable = false)
  private String clienteTelefono;

  @Column(length = 10, nullable = false)
  private String placa;

  @Column(length = 60, nullable = false)
  private String marca;

  @Column(length = 60, nullable = false)
  private String modelo;

  @Column(name = "aseguradora_codigo", length = 40)
  private String aseguradoraCodigo;

  @Column(name = "aseguradora_nombre", length = 150)
  private String aseguradoraNombre;

  @Column(length = 2000, nullable = false)
  private String descripcion;

  @Enumerated(EnumType.STRING)
  @Column(length = 30, nullable = false)
  private EstadoOrden estado;

  @JdbcTypeCode(SqlTypes.TIMESTAMP)
  @Column(name = "fecha_creacion", nullable = false, updatable = false)
  private Instant fechaCreacion;

  @Version
  @Column(nullable = false)
  private Long version;

  public static OrdenJpaEntity desde(OrdenServicio o) {
    var e = new OrdenJpaEntity();
    e.id = o.getId();
    e.clienteNombre = o.getCliente().nombre();
    e.clienteDocumento = o.getCliente().documento();
    e.clienteTelefono = o.getCliente().telefono();
    e.placa = o.getVehiculo().placa();
    e.marca = o.getVehiculo().marca();
    e.modelo = o.getVehiculo().modelo();
    if (o.getAseguradora() != null) {
      e.aseguradoraCodigo = o.getAseguradora().codigo();
      e.aseguradoraNombre = o.getAseguradora().nombre();
    }
    e.descripcion = o.getDescripcion();
    e.estado = o.getEstado();
    e.fechaCreacion = o.getFechaCreacion();
    e.version = o.getVersion();
    return e;
  }

  public OrdenServicio aDominio() {
    return OrdenServicio.reconstituir(
        id,
        new Cliente(clienteNombre, clienteDocumento, clienteTelefono),
        new Vehiculo(placa, marca, modelo),
        aseguradoraCodigo == null ? null : new Aseguradora(aseguradoraCodigo, aseguradoraNombre),
        descripcion,
        estado,
        fechaCreacion,
        version);
  }
}
