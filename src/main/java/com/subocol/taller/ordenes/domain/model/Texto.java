package com.subocol.taller.ordenes.domain.model;

final class Texto {
  private Texto() {}

  static String requerido(String value, int max, String field) {
    if (value == null || value.isBlank() || value.length() > max)
      throw new IllegalArgumentException(
          field + " es obligatorio y admite máximo " + max + " caracteres");
    return value.strip();
  }
}
