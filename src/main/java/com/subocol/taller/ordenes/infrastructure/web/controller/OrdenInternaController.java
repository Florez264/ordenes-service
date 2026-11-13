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

  public record Resumen(UUID id, String estado, String telefono) {}

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<Resumen>> consultar(@PathVariable UUID id) {
    var o = consultar.ejecutar(id);
    return ApiResponses.ok(new Resumen(o.id(), o.estado().name(), o.cliente().telefono()));
  }
}
