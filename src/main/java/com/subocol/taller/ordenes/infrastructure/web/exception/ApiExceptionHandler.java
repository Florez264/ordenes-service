package com.subocol.taller.ordenes.infrastructure.web.exception;

import com.subocol.taller.ordenes.domain.exception.*;
import com.subocol.taller.ordenes.infrastructure.web.response.*;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
  @ExceptionHandler(RecursoNoEncontradoException.class)
  public ResponseEntity<ApiResponse<ErrorData>> missing(RecursoNoEncontradoException ex) {
    return ApiResponses.error(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(ReglaNegocioException.class)
  public ResponseEntity<ApiResponse<ErrorData>> business(ReglaNegocioException ex) {
    return ApiResponses.error(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<ErrorData>> invalid(IllegalArgumentException ex) {
    return ApiResponses.error(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos");
  }

  @ExceptionHandler({
    DataIntegrityViolationException.class,
    OptimisticLockingFailureException.class,
    PessimisticLockingFailureException.class
  })
  public ResponseEntity<ApiResponse<ErrorData>> conflict(Exception ex) {
    return ApiResponses.error(
        HttpStatus.CONFLICT, "La operación entra en conflicto con los datos actuales");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<ErrorData>> unexpected(Exception ex) {
    log.error("Error no controlado", ex);
    return ApiResponses.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    Map<String, String> fields = new LinkedHashMap<>();
    if (ex instanceof MethodArgumentNotValidException validation)
      validation
          .getBindingResult()
          .getFieldErrors()
          .forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
    String message =
        switch (status.value()) {
          case 400 -> "Datos de entrada inválidos";
          case 401 -> "Autenticación requerida";
          case 403 -> "Acceso denegado";
          case 404 -> "Recurso no encontrado";
          case 405 -> "Método HTTP no permitido";
          case 409 -> "Conflicto con los datos existentes";
          case 415 -> "Tipo de contenido no soportado";
          default ->
              status.is5xxServerError()
                  ? "Error interno del servidor"
                  : "No se pudo procesar la petición";
        };
    return new ResponseEntity<>(
        new ApiResponse<>(status.value(), new ErrorData(message, fields)), headers, status);
  }
}
