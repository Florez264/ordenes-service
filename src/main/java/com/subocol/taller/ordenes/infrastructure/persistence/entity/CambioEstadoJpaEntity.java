package com.subocol.taller.ordenes.infrastructure.persistence.entity;

import com.subocol.taller.ordenes.domain.model.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "historial_estado")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CambioEstadoJpaEntity {
  @Id
  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(length = 36)
  private UUID id;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "orden_id", length = 36, nullable = false)
  private UUID ordenId;

  @Enumerated(EnumType.STRING)
  @Column(length = 30)
  private EstadoOrden anterior;

  @Enumerated(EnumType.STRING)
  @Column(length = 30, nullable = false)
  private EstadoOrden nuevo;

  @Column(length = 500, nullable = false)
  private String motivo;

  @Column(length = 100, nullable = false)
  private String actor;

  @JdbcTypeCode(SqlTypes.TIMESTAMP)
  @Column(nullable = false)
  private Instant fecha;

  @Column(nullable = false)
  private long secuencia;

  public static CambioEstadoJpaEntity desde(CambioEstado c) {
    var e = new CambioEstadoJpaEntity();
    e.id = c.id();
    e.ordenId = c.ordenId();
    e.anterior = c.anterior();
    e.nuevo = c.nuevo();
    e.motivo = c.motivo();
    e.actor = c.actor();
    e.fecha = c.fecha();
    e.secuencia = c.secuencia();
    return e;
  }

  public CambioEstado aDominio() {
    return new CambioEstado(id, ordenId, anterior, nuevo, motivo, actor, fecha, secuencia);
  }
}
