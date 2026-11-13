package com.subocol.taller.ordenes.infrastructure.configuration;

import static org.assertj.core.api.Assertions.*;

import com.subocol.taller.ordenes.infrastructure.configuration.SeguridadConfiguration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.mock.web.MockHttpServletRequest;

class CorsCloudConfigurationTest {
  @Test
  void cloudPermiteFrontendEstableYConservaOrigenConfiguradoSinAbrirInternos() throws Exception {
    var propiedades =
        PropertiesLoaderUtils.loadProperties(new ClassPathResource("application-cloud.properties"));
    var environment = new StandardEnvironment();
    environment
        .getPropertySources()
        .addFirst(
            new MapPropertySource(
                "prueba",
                Map.of(
                    "CORS_ALLOWED_ORIGINS",
                    "http://localhost:4200",
                    "FRONTEND_ORIGIN",
                    "https://subocol-taller.vercel.app")));
    var origenes =
        environment.resolveRequiredPlaceholders(
            propiedades.getProperty("taller.cors.allowed-origins"));
    var source = new SeguridadConfiguration().corsConfigurationSource(origenes);
    var config =
        source.getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/api/ordenes/listar"));
    assertThat(config).isNotNull();
    assertThat(config.checkOrigin("https://subocol-taller.vercel.app"))
        .isEqualTo("https://subocol-taller.vercel.app");
    assertThat(config.checkOrigin("http://localhost:4200")).isEqualTo("http://localhost:4200");
    assertThat(config.checkOrigin("https://externo.example")).isNull();
    assertThat(
            source.getCorsConfiguration(
                new MockHttpServletRequest("OPTIONS", "/internal/ordenes/1")))
        .isNull();
  }

  @Test
  void frontendPredeterminadoFuncionaSinVariablesCors() throws Exception {
    var propiedades =
        PropertiesLoaderUtils.loadProperties(new ClassPathResource("application-cloud.properties"));
    var resolver = new org.springframework.util.PropertyPlaceholderHelper("${", "}", ":", null, false);
    var origenes =
        resolver.replacePlaceholders(
            propiedades.getProperty("taller.cors.allowed-origins"), key -> null);
    var source = new SeguridadConfiguration().corsConfigurationSource(origenes);
    var config =
        source.getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/api/ordenes/listar"));
    assertThat(config.checkOrigin("https://subocol-taller.vercel.app"))
        .isEqualTo("https://subocol-taller.vercel.app");
  }
}
