package com.subocol.taller.ordenes.application.dto;

import com.subocol.taller.ordenes.domain.model.*;

public record RegistrarOrden(
    Cliente cliente, Vehiculo vehiculo, Aseguradora aseguradora, String descripcion) {}
