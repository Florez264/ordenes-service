package com.subocol.taller.ordenes.application.port;

import com.subocol.taller.ordenes.application.dto.*;

public interface ConsultaOrdenesPort {
  Pagina<OrdenDetalle> listar(ListarOrdenes query);
}
