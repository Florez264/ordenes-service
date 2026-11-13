package com.subocol.taller.ordenes.infrastructure.persistence.adapter;

import com.subocol.taller.ordenes.application.port.CierresOrdenPort;
import com.subocol.taller.ordenes.infrastructure.persistence.entity.CierreOrdenJpaEntity;
import jakarta.persistence.EntityManager;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class CierresOrdenAdapter implements CierresOrdenPort {
  private final EntityManager em;

  @Transactional
  public void registrar(UUID ordenId, long version) {
    em.persist(CierreOrdenJpaEntity.crear(ordenId, version));
    em.flush();
  }

  @Transactional(readOnly = true)
  public List<Cierre> pendientes() {
    return em
        .createQuery(
            "select c from CierreOrdenJpaEntity c where c.confirmado=false order by"
                + " c.ordenVersion,c.id",
            CierreOrdenJpaEntity.class)
        .setMaxResults(20)
        .getResultList()
        .stream()
        .map(c -> new Cierre(c.getId(), c.getOrdenId(), c.getOrdenVersion()))
        .toList();
  }

  @Transactional
  public void confirmar(UUID id) {
    em.createQuery("update CierreOrdenJpaEntity c set c.confirmado=true where c.id=:id")
        .setParameter("id", id)
        .executeUpdate();
  }
}
