package com.subocol.taller.ordenes.infrastructure.persistence.repository;

import com.subocol.taller.ordenes.domain.model.EstadoOrden;
import com.subocol.taller.ordenes.infrastructure.persistence.entity.OrdenJpaEntity;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface SpringDataOrdenRepository extends JpaRepository<OrdenJpaEntity, UUID> {
  @Query(
      "select o from OrdenJpaEntity o where (:estado is null or o.estado=:estado) and (:placa is"
          + " null or o.placa=:placa)")
  Page<OrdenJpaEntity> buscar(
      @Param("estado") EstadoOrden estado, @Param("placa") String placa, Pageable pageable);
}
