package com.subocol.taller.ordenes.infrastructure.web.controller;

import com.subocol.taller.ordenes.application.usecase.ConsultarOrdenUseCase;
import com.subocol.taller.ordenes.infrastructure.web.response.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/ordenes")
@RequiredArgsConstructor
public class OrdenInternaController {
  private final ConsultarOrdenUseCase consultar;
  private final com.subocol.taller.ordenes.application.port.CierresOrdenPort cierres;

  public record Resumen(UUID id, String estado, String telefono, long version) {}

  @GetMapping("/cierres-pendientes")
  public ResponseEntity<?> cierres() {
    return ApiResponses.ok(cierres.pendientes());
  }

  @PatchMapping("/cierres/{id}/confirmar")
  public ResponseEntity<?> confirmar(@PathVariable UUID id) {
    cierres.confirmar(id);
    return ApiResponses.ok(java.util.Map.of("confirmado", true));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<Resumen>> consultar(@PathVariable UUID id) {
    var o = consultar.ejecutar(id);
    return ApiResponses.ok(
        new Resumen(o.id(), o.estado().name(), o.cliente().telefono(), o.version()));
  }
}
