package com.subocol.taller.ordenes.infrastructure.persistence.adapter;

import com.subocol.taller.ordenes.application.dto.*;
import com.subocol.taller.ordenes.application.port.ConsultaOrdenesPort;
import com.subocol.taller.ordenes.domain.model.OrdenServicio;
import com.subocol.taller.ordenes.domain.repository.OrdenRepository;
import com.subocol.taller.ordenes.infrastructure.persistence.entity.OrdenJpaEntity;
import com.subocol.taller.ordenes.infrastructure.persistence.repository.SpringDataOrdenRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrdenPersistenceAdapter implements OrdenRepository, ConsultaOrdenesPort {
  private final SpringDataOrdenRepository repository;

  @Override
  @Transactional
  public OrdenServicio guardar(OrdenServicio orden) {
    return repository.saveAndFlush(OrdenJpaEntity.desde(orden)).aDominio();
  }

  @Override
  public Optional<OrdenServicio> buscarPorId(UUID id) {
    return repository.findById(id).map(OrdenJpaEntity::aDominio);
  }

  @Override
  public Pagina<OrdenDetalle> listar(ListarOrdenes query) {
    var pageable =
        PageRequest.of(
            query.page(), query.size(), Sort.by(Sort.Direction.DESC, "fechaCreacion", "id"));
    var page = repository.buscar(query.estado(), query.placa(), pageable);
    return new Pagina<>(
        page.getContent().stream().map(e -> OrdenDetalle.desde(e.aDominio())).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast());
  }
}
