package com.subocol.taller.ordenes.domain.repository;

import com.subocol.taller.ordenes.domain.model.OrdenServicio;
import java.util.*;

public interface OrdenRepository {
  OrdenServicio guardar(OrdenServicio orden);

  Optional<OrdenServicio> buscarPorId(UUID id);
}
