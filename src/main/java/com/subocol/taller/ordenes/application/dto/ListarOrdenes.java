package com.subocol.taller.ordenes.application.dto;

import com.subocol.taller.ordenes.domain.model.EstadoOrden;
import java.util.Locale;

public record ListarOrdenes(int page, int size, EstadoOrden estado, String placa) {
  public ListarOrdenes {
    if (page < 0 || size < 1 || size > 50 || (long) page * size > Integer.MAX_VALUE)
      throw new IllegalArgumentException("Página inválida: tamaño entre 1 y 50");
    if (placa != null) {
      placa = placa.strip().toUpperCase(Locale.ROOT);
      if (!placa.matches("[A-Z0-9]{5,10}"))
        throw new IllegalArgumentException("Filtro de placa inválido");
    }
  }
}
