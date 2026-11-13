package com.subocol.taller.ordenes.infrastructure.web.request;

import com.subocol.taller.ordenes.application.dto.RegistrarOrden;
import com.subocol.taller.ordenes.domain.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record CrearOrdenRequest(
    @NotNull @Valid ClienteRequest cliente,
    @NotNull @Valid VehiculoRequest vehiculo,
    @Valid AseguradoraRequest aseguradora,
    @NotBlank @Size(max = 2000) String descripcion) {
  public record ClienteRequest(
      @NotBlank @Size(max = 150) String nombre,
      @NotBlank @Size(max = 30) String documento,
      @NotBlank
          @Pattern(
              regexp = "\\+?[0-9]{7,15}",
              message = "Debe contener de 7 a 15 dígitos, con + opcional")
          String telefono) {}

  public record VehiculoRequest(
      @NotBlank @Pattern(regexp = "[A-Za-z0-9]{5,10}") String placa,
      @NotBlank @Size(max = 60) String marca,
      @NotBlank @Size(max = 60) String modelo) {}

  public record AseguradoraRequest(
      @NotBlank @Size(max = 40) String codigo, @NotBlank @Size(max = 150) String nombre) {}

  public RegistrarOrden aComando() {
    return new RegistrarOrden(
        new Cliente(cliente.nombre(), cliente.documento(), cliente.telefono()),
        new Vehiculo(vehiculo.placa(), vehiculo.marca(), vehiculo.modelo()),
        aseguradora == null ? null : new Aseguradora(aseguradora.codigo(), aseguradora.nombre()),
        descripcion);
  }
}
