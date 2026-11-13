package com.subocol.taller.ordenes.infrastructure.configuration;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@org.springframework.boot.context.properties.EnableConfigurationProperties(CuentasAsesor.class)
public class SeguridadConfiguration {
  @Bean
  UserDetailsService usuarios(
      CuentasAsesor cuentas,
      @Value("${taller.internal.usuario}") String interno,
      @Value("${taller.internal.password}") String secreto) {
    var encoder = new BCryptPasswordEncoder();
    var usuarios =
        new java.util.ArrayList<org.springframework.security.core.userdetails.UserDetails>();
    for (var cuenta : cuentas.asesores())
      usuarios.add(
          User.withUsername(cuenta.usuario())
              .password("{bcrypt}" + encoder.encode(cuenta.password()))
              .roles("ASESOR")
              .build());
    usuarios.add(
        User.withUsername(interno)
            .password("{bcrypt}" + encoder.encode(secreto))
            .roles("SERVICIO")
            .build());
    return new InMemoryUserDetailsManager(usuarios);
  }

  @Bean
  SecurityFilterChain seguridad(HttpSecurity http) throws Exception {
    return http.cors(org.springframework.security.config.Customizer.withDefaults())
        .csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/actuator/health", "/actuator/health/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/internal/**")
                    .hasRole("SERVICIO")
                    .requestMatchers("/api/**")
                    .hasRole("ASESOR")
                    .anyRequest()
                    .denyAll())
        .httpBasic(
            b -> b.authenticationEntryPoint((r, s, e) -> error(s, 401, "Autenticación requerida")))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint((r, s, x) -> error(s, 401, "Autenticación requerida"))
                    .accessDeniedHandler((r, s, x) -> error(s, 403, "Acceso denegado")))
        .build();
  }

  @Bean
  org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource(
      @Value("${taller.cors.allowed-origins:}") String origins) {
    var config = new org.springframework.web.cors.CorsConfiguration();
    config.setAllowedOrigins(java.util.Arrays.stream(origins.split(","))
        .map(String::trim).filter(origin -> !origin.isEmpty()).toList());
    if (config.getAllowedOrigins().stream().anyMatch(origin -> origin.contains("*"))) {
      throw new IllegalArgumentException("CORS requiere origenes explicitos, sin comodines");
    }
    config.setAllowedMethods(java.util.List.of("GET", "POST", "PATCH", "OPTIONS"));
    config.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Accept"));
    config.setAllowCredentials(false);
    config.setMaxAge(3600L);
    var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
  }

  private static void error(HttpServletResponse response, int status, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    if (status == 401) response.setHeader("WWW-Authenticate", "Basic realm=\"subocol\"");
    response
        .getWriter()
        .write("{\"status\":" + status + ",\"data\":{\"message\":\"" + message + "\"}}");
  }
}
