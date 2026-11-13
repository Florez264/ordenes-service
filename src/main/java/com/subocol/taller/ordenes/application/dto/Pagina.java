package com.subocol.taller.ordenes.application.dto;

import java.util.List;

/** Contrato propio, independiente de Page y Pageable de Spring. */
public record Pagina<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last) {
  public Pagina {
    content = List.copyOf(content);
  }
}
