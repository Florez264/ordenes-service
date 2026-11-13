package com.subocol.taller.ordenes.infrastructure.configuration;

import com.subocol.taller.ordenes.application.port.UnidadTrabajoPort;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
public class UnidadTrabajoAdapter implements UnidadTrabajoPort {
  private final TransactionTemplate transactionTemplate;

  public <T> T ejecutar(Supplier<T> operacion) {
    return transactionTemplate.execute(status -> operacion.get());
  }
}
