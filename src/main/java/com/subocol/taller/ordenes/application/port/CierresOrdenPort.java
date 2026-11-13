package com.subocol.taller.ordenes.application.port;

import java.util.*;

public interface CierresOrdenPort {
  record Cierre(UUID id, UUID ordenId, long version) {}

  void registrar(UUID ordenId, long version);

  List<Cierre> pendientes();

  void confirmar(UUID id);
}
