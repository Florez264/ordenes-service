package com.subocol.taller.ordenes.infrastructure.persistence.adapter;

import com.subocol.taller.ordenes.application.dto.Pagina;
import com.subocol.taller.ordenes.application.port.HistorialOrdenPort;
import com.subocol.taller.ordenes.domain.model.CambioEstado;
import com.subocol.taller.ordenes.infrastructure.persistence.entity.CambioEstadoJpaEntity;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistorialOrdenAdapter implements HistorialOrdenPort {
  private final EntityManager em;

  @Transactional
  public void agregar(CambioEstado cambio) {
    em.persist(CambioEstadoJpaEntity.desde(cambio));
    em.flush();
  }

  public Pagina<CambioEstado> listar(UUID id, int page, int size) {
    var filas =
        em.createQuery(
                "select h from CambioEstadoJpaEntity h where h.ordenId=:id order by h.secuencia"
                    + " asc",
                CambioEstadoJpaEntity.class)
            .setParameter("id", id)
            .setFirstResult(Math.multiplyExact(page, size))
            .setMaxResults(size)
            .getResultList();
    long total =
        em.createQuery(
                "select count(h) from CambioEstadoJpaEntity h where h.ordenId=:id", Long.class)
            .setParameter("id", id)
            .getSingleResult();
    int pages = (int) ((total + size - 1) / size);
    return new Pagina<>(
        filas.stream().map(CambioEstadoJpaEntity::aDominio).toList(),
        page,
        size,
        total,
        pages,
        page == 0,
        page >= pages - 1);
  }
}
