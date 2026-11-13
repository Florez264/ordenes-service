package com.subocol.taller.ordenes.domain.model;

public enum EstadoOrden {
  REGISTRADA,
  EN_DIAGNOSTICO,
  PENDIENTE_AUTORIZACION,
  EN_REPARACION,
  REPARADA,
  CERRADA;

  public boolean permite(EstadoOrden destino) {
    return switch (this) {
      case REGISTRADA -> destino == EN_DIAGNOSTICO;
      case EN_DIAGNOSTICO -> destino == PENDIENTE_AUTORIZACION || destino == EN_REPARACION;
      case PENDIENTE_AUTORIZACION -> destino == EN_REPARACION;
      case EN_REPARACION -> destino == REPARADA;
      case REPARADA -> destino == CERRADA;
      case CERRADA -> false;
    };
  }
}
