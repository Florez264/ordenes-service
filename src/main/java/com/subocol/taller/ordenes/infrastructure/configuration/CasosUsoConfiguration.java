package com.subocol.taller.ordenes.infrastructure.configuration;

import com.subocol.taller.ordenes.application.port.*;
import com.subocol.taller.ordenes.application.usecase.*;
import com.subocol.taller.ordenes.domain.repository.OrdenRepository;
import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class CasosUsoConfiguration {
  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  PoliticaRecepcionOrdenPort politica(@Value("${taller.aseguradoras-integradas:}") String codigos) {
    Set<String> integradas =
        Arrays.stream(codigos.split(","))
            .map(String::strip)
            .filter(s -> !s.isEmpty())
            .map(s -> s.toUpperCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());
    return codigo -> !integradas.contains(codigo);
  }

  @Bean
  RegistrarOrdenUseCase registrar(
      OrdenRepository repo,
      PoliticaRecepcionOrdenPort politica,
      Clock clock,
      HistorialOrdenPort historial,
      ActorActualPort actor,
      UnidadTrabajoPort unidad) {
    return new RegistrarOrdenUseCase(repo, politica, clock, historial, actor, unidad);
  }

  @Bean
  ConsultarOrdenUseCase consultar(OrdenRepository repo) {
    return new ConsultarOrdenUseCase(repo);
  }

  @Bean
  ListarOrdenesUseCase listar(ConsultaOrdenesPort port) {
    return new ListarOrdenesUseCase(port);
  }

  @Bean
  org.springframework.transaction.support.TransactionTemplate transactionTemplate(
      org.springframework.transaction.PlatformTransactionManager manager) {
    return new org.springframework.transaction.support.TransactionTemplate(manager);
  }

  @Bean
  CambiarEstadoUseCase cambiar(
      OrdenRepository repo,
      HistorialOrdenPort historial,
      ActorActualPort actor,
      UnidadTrabajoPort unidad,
      Clock clock) {
    return new CambiarEstadoUseCase(repo, historial, actor, unidad, clock);
  }

  @Bean
  ConsultarHistorialUseCase historial(OrdenRepository repo, HistorialOrdenPort historial) {
    return new ConsultarHistorialUseCase(repo, historial);
  }
}
