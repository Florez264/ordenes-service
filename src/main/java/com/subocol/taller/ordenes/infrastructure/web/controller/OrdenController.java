package com.subocol.taller.ordenes.infrastructure.web.controller;

import com.subocol.taller.ordenes.application.dto.*;
import com.subocol.taller.ordenes.application.usecase.*;
import com.subocol.taller.ordenes.domain.model.EstadoOrden;
import com.subocol.taller.ordenes.infrastructure.web.request.CrearOrdenRequest;
import com.subocol.taller.ordenes.infrastructure.web.response.*;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
public class OrdenController {
  private final RegistrarOrdenUseCase registrar;
  private final ConsultarOrdenUseCase consultar;
  private final ListarOrdenesUseCase listar;

  public record OrdenCreada(UUID id, EstadoOrden estado, String message) {}

  @PostMapping("/crear")
  public ResponseEntity<ApiResponse<OrdenCreada>> crear(
      @Valid @RequestBody CrearOrdenRequest request) {
    var orden = registrar.ejecutar(request.aComando());
    return ApiResponses.created(
        new OrdenCreada(orden.id(), orden.estado(), "Orden creada correctamente"));
  }

  @GetMapping("/consultar/{id}")
  public ResponseEntity<ApiResponse<OrdenDetalle>> consultar(@PathVariable UUID id) {
    return ApiResponses.ok(consultar.ejecutar(id));
  }

  @GetMapping("/listar")
  public ResponseEntity<ApiResponse<Pagina<OrdenDetalle>>> listar(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) EstadoOrden estado,
      @RequestParam(required = false) String placa) {
    return ApiResponses.ok(listar.ejecutar(new ListarOrdenes(page, size, estado, placa)));
  }

  private final CambiarEstadoUseCase cambiarEstado;
  private final ConsultarHistorialUseCase consultarHistorial;

  public record CambioRequest(
      @jakarta.validation.constraints.NotNull EstadoOrden estado,
      @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 500)
          String motivo,
      @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.PositiveOrZero
          Long version) {}

  public record ReabrirRequest(
      @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 500)
          String motivo,
      @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.PositiveOrZero
          Long version) {}

  public record EstadoActualizado(
      boolean update, UUID id, EstadoOrden estado, long version, String message) {}

  @PatchMapping("/{id}/cambiar-estado")
  public ResponseEntity<ApiResponse<EstadoActualizado>> cambiar(
      @PathVariable UUID id, @Valid @RequestBody CambioRequest request) {
    var o =
        cambiarEstado.ejecutar(id, request.estado(), request.motivo(), request.version(), false);
    return ApiResponses.ok(
        new EstadoActualizado(
            true, o.id(), o.estado(), o.version(), "Estado actualizado correctamente"));
  }

  @PatchMapping("/{id}/reabrir")
  public ResponseEntity<ApiResponse<EstadoActualizado>> reabrir(
      @PathVariable UUID id, @Valid @RequestBody ReabrirRequest request) {
    var o = cambiarEstado.ejecutar(id, null, request.motivo(), request.version(), true);
    return ApiResponses.ok(
        new EstadoActualizado(
            true, o.id(), o.estado(), o.version(), "Orden reabierta correctamente"));
  }

  @GetMapping("/{id}/historial")
  public ResponseEntity<ApiResponse<Pagina<com.subocol.taller.ordenes.domain.model.CambioEstado>>>
      historial(
          @PathVariable UUID id,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "20") int size) {
    return ApiResponses.ok(consultarHistorial.ejecutar(id, page, size));
  }
}
