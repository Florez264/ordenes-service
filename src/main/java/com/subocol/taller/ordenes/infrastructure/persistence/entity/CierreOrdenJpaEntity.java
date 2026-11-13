package com.subocol.taller.ordenes.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "cierre_orden")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CierreOrdenJpaEntity {
  @Id
  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(length = 36)
  private UUID id;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "orden_id", length = 36, nullable = false)
  private UUID ordenId;

  @Column(name = "orden_version", nullable = false)
  private long ordenVersion;

  @Column(nullable = false)
  private boolean confirmado;

  public static CierreOrdenJpaEntity crear(UUID ordenId, long version) {
    var e = new CierreOrdenJpaEntity();
    e.id = UUID.randomUUID();
    e.ordenId = ordenId;
    e.ordenVersion = version;
    return e;
  }
}
