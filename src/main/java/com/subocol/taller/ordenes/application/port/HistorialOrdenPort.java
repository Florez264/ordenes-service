package com.subocol.taller.ordenes.application.port;

import com.subocol.taller.ordenes.application.dto.Pagina;
import com.subocol.taller.ordenes.domain.model.CambioEstado;
import java.util.UUID;

public interface HistorialOrdenPort {
  void agregar(CambioEstado cambio);

  Pagina<CambioEstado> listar(UUID ordenId, int page, int size);
}
