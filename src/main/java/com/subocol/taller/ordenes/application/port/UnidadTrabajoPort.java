package com.subocol.taller.ordenes.application.port;

import java.util.function.Supplier;

public interface UnidadTrabajoPort {
  <T> T ejecutar(Supplier<T> operacion);
}
