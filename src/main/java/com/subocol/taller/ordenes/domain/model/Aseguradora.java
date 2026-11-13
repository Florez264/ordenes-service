package com.subocol.taller.ordenes.domain.model;

import java.util.Locale;

public record Aseguradora(String codigo, String nombre) {
  public Aseguradora {
    codigo = Texto.requerido(codigo, 40, "Código de aseguradora").toUpperCase(Locale.ROOT);
    nombre = Texto.requerido(nombre, 150, "Nombre de aseguradora");
  }
}
