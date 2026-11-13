package com.subocol.taller.ordenes.domain.model;

public record Cliente(String nombre, String documento, String telefono) {
  public Cliente {
    nombre = Texto.requerido(nombre, 150, "Nombre");
    documento = Texto.requerido(documento, 30, "Documento");
    telefono = Texto.requerido(telefono, 25, "Teléfono");
    if (!telefono.matches("\\+?[0-9]{7,15}"))
      throw new IllegalArgumentException("Teléfono inválido");
  }
}
