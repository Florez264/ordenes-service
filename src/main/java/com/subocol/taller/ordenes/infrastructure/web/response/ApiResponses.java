package com.subocol.taller.ordenes.infrastructure.web.response;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.*;

/** Construcción explícita: respeta archivos y respuestas sin cuerpo. */
public final class ApiResponses {
  private ApiResponses() {}

  public record Updated(UUID id, boolean update, String message) {}

  public record Deleted(UUID id, boolean delete, String message) {}

  public static <T> ResponseEntity<ApiResponse<T>> of(HttpStatusCode status, T data) {
    if (data instanceof ApiResponse<?>) throw new IllegalArgumentException("Respuesta ya envuelta");
    if (status.is1xxInformational() || status.value() == 204 || status.value() == 304)
      throw new IllegalArgumentException("Este estado HTTP no admite cuerpo");
    return ResponseEntity.status(status).body(new ApiResponse<>(status.value(), data));
  }

  public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
    return of(HttpStatus.OK, data);
  }

  public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
    return of(HttpStatus.CREATED, data);
  }

  public static ResponseEntity<ApiResponse<Updated>> updated(UUID id, String message) {
    return ok(new Updated(id, true, message));
  }

  public static ResponseEntity<ApiResponse<Deleted>> deleted(UUID id, String message) {
    return ok(new Deleted(id, true, message));
  }

  public static ResponseEntity<ApiResponse<ErrorData>> error(
      HttpStatusCode status, String message) {
    return of(status, new ErrorData(message, Map.of()));
  }
}
