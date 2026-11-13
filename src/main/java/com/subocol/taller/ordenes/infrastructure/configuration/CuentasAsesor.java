package com.subocol.taller.ordenes.infrastructure.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "taller.auth")
@Validated
public record CuentasAsesor(@NotEmpty List<@Valid Cuenta> asesores) {
  public CuentasAsesor {
    if (asesores != null) asesores = List.copyOf(asesores);
  }

  public record Cuenta(
      @NotBlank @Size(max = 100) String usuario,
      @NotBlank @Size(min = 8, max = 72) String password) {}
}
