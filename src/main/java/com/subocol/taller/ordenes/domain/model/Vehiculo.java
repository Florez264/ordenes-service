package com.subocol.taller.ordenes.domain.model;

import java.util.Locale;

public record Vehiculo(String placa, String marca, String modelo) {
  public Vehiculo {
    placa = Texto.requerido(placa, 10, "Placa").toUpperCase(Locale.ROOT);
    if (!placa.matches("[A-Z0-9]{5,10}")) throw new IllegalArgumentException("Placa inválida");
    marca = Texto.requerido(marca, 60, "Marca");
    modelo = Texto.requerido(modelo, 60, "Modelo");
  }
}
