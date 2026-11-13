package com.subocol.taller.ordenes;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import com.subocol.taller.ordenes.infrastructure.persistence.repository.SpringDataOrdenRepository;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "taller.cors.allowed-origins=http://localhost:4200")
@AutoConfigureMockMvc
class OrdenHttpIntegrationTest {
  @Test
  void corsPermitePreflightAngularSinAutenticacion() throws Exception {
    mvc.perform(options("/api/prueba")
        .header("Origin", "http://localhost:4200")
        .header("Access-Control-Request-Method", "PATCH")
        .header("Access-Control-Request-Headers", "authorization,content-type"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
        .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
  }

  @Test
  void corsRechazaOrigenExternoYRutasInternas() throws Exception {
    mvc.perform(options("/api/prueba")
        .header("Origin", "https://externo.example")
        .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    mvc.perform(options("/internal/ordenes/1")
        .header("Origin", "http://localhost:4200")
        .header("Access-Control-Request-Method", "GET"))
        .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
  }

  @Test
  void corsMantieneAutenticacionEnSolicitudesReales() throws Exception {
    mvc.perform(get("/api/prueba").header("Origin", "http://localhost:4200"))
        .andExpect(status().isUnauthorized())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
  }

  @Autowired MockMvc mvc;
  @Autowired SpringDataOrdenRepository repository;
  private static final String BODY =
      """
      {"cliente":{"nombre":"Ana","documento":"123","telefono":"+573001234567"},
       "vehiculo":{"placa":"abc123","marca":"Toyota","modelo":"Corolla"},"descripcion":"Revisar frenos"}
      """;

  @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;

  @BeforeEach
  void limpiar() {
    jdbc.update("delete from historial_estado");
    repository.deleteAll();
  }

  private String crear(String body) throws Exception {
    var json =
        mvc.perform(
                autenticadoPost("/api/ordenes/crear")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.data.estado").value("REGISTRADA"))
            .andExpect(jsonPath("$.data.message").value("Orden creada correctamente"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JsonPath.read(json, "$.data.id");
  }

  @Test
  void creaConsultaYListaConFiltrosYPaginacionReal() throws Exception {
    var first = crear(BODY);
    crear(BODY.replace("abc123", "XYZ789"));
    mvc.perform(autenticadoGet("/api/ordenes/consultar/" + first))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.data.vehiculo.placa").value("ABC123"))
        .andExpect(jsonPath("$.data.version").value(0));
    mvc.perform(autenticadoGet("/api/ordenes/listar").param("page", "0").param("size", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content.length()").value(1))
        .andExpect(jsonPath("$.data.totalElements").value(2))
        .andExpect(jsonPath("$.data.totalPages").value(2))
        .andExpect(jsonPath("$.data.first").value(true))
        .andExpect(jsonPath("$.data.last").value(false));
    mvc.perform(
            autenticadoGet("/api/ordenes/listar")
                .param("placa", "abc123")
                .param("estado", "REGISTRADA"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalElements").value(1))
        .andExpect(jsonPath("$.data.content[0].id").value(first));
    mvc.perform(autenticadoGet("/api/ordenes/listar").param("estado", "CERRADA"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content").isEmpty());
  }

  @Test
  void validaDatosYNoGuardaOrdenesInvalidas() throws Exception {
    mvc.perform(
            autenticadoPost("/api/ordenes/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY.replace("Ana", " ")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.data.fields['cliente.nombre']").exists());
    mvc.perform(
            autenticadoPost("/api/ordenes/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data.fields.cliente").exists());
    mvc.perform(autenticadoGet("/api/ordenes/listar").param("size", "51"))
        .andExpect(status().isBadRequest());
    mvc.perform(autenticadoGet("/api/ordenes/listar").param("page", "-1"))
        .andExpect(status().isBadRequest());
    mvc.perform(autenticadoGet("/api/ordenes/listar").param("estado", "INEXISTENTE"))
        .andExpect(status().isBadRequest());
    assertThat(repository.count()).isZero();
  }

  @Test
  void notFoundYJsonInvalidoMantienenElContrato() throws Exception {
    mvc.perform(autenticadoGet("/api/ordenes/consultar/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.data.message").value("Orden no encontrada"));
    mvc.perform(autenticadoGet("/api/ordenes/consultar/id-invalido"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
    mvc.perform(
            autenticadoPost("/api/ordenes/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{broken"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void bloqueaRegistroManualDeAseguradoraIntegrada() throws Exception {
    var body =
        """
        {"cliente":{"nombre":"Ana","documento":"123","telefono":"+573001234567"},
         "vehiculo":{"placa":"abc123","marca":"Toyota","modelo":"Corolla"},
         "aseguradora":{"codigo":"INTEGRADA_DEMO","nombre":"Demo"},"descripcion":"Frenos"}
        """;
    mvc.perform(
            autenticadoPost("/api/ordenes/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409));
    assertThat(repository.count()).isZero();
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder autenticadoGet(
      String uri) {
    return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(uri)
        .with(
            org.springframework.security.test.web.servlet.request
                .SecurityMockMvcRequestPostProcessors.httpBasic("asesor", "asesor-local-password"));
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
      autenticadoPost(String uri) {
    return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(uri)
        .with(
            org.springframework.security.test.web.servlet.request
                .SecurityMockMvcRequestPostProcessors.httpBasic("asesor", "asesor-local-password"));
  }
}
