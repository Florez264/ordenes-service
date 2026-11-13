package com.subocol.taller.ordenes.application.usecase;

import com.subocol.taller.ordenes.application.dto.*;
import com.subocol.taller.ordenes.application.port.ConsultaOrdenesPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ListarOrdenesUseCase {
  private final ConsultaOrdenesPort consultas;

  public Pagina<OrdenDetalle> ejecutar(ListarOrdenes query) {
    return consultas.listar(query);
  }
}
