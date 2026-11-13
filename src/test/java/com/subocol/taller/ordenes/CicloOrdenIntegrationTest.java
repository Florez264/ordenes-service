package com.subocol.taller.ordenes;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import com.subocol.taller.ordenes.infrastructure.persistence.adapter.HistorialOrdenAdapter;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CicloOrdenIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
  com.subocol.taller.ordenes.infrastructure.persistence.adapter.CierresOrdenAdapter cierres;

  @MockitoSpyBean HistorialOrdenAdapter historial;
  static final String BODY =
      """
      {"cliente":{"nombre":"Ana","documento":"123","telefono":"3001234567"},"vehiculo":{"placa":"ABC123","marca":"Toyota","modelo":"2020"},"descripcion":"Frenos"}
      """;

  @BeforeEach
  void limpiar() {
    jdbc.update("delete from historial_estado");
    jdbc.update("delete from orden_servicio");
  }

  String crear() throws Exception {
    return JsonPath.read(
        mvc.perform(
                post("/api/ordenes/crear")
                    .with(user("juan").roles("ASESOR"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(BODY))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString(),
        "$.data.id");
  }

  void cambiar(String id, String estado, long version) throws Exception {
    mvc.perform(
            patch("/api/ordenes/" + id + "/cambiar-estado")
                .with(user("juan").roles("ASESOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"estado\":\""
                        + estado
                        + "\",\"motivo\":\"Avance verificado\",\"version\":"
                        + version
                        + "}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.version").value(version + 1));
  }

  @Test
  void cicloCompletoReaperturaYAutorReal() throws Exception {
    String id = crear();
    cambiar(id, "EN_DIAGNOSTICO", 0);
    cambiar(id, "PENDIENTE_AUTORIZACION", 1);
    cambiar(id, "EN_REPARACION", 2);
    cambiar(id, "REPARADA", 3);
    cambiar(id, "CERRADA", 4);
    mvc.perform(
            patch("/api/ordenes/" + id + "/reabrir")
                .with(user("supervisor").roles("ASESOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"motivo\":\"Revisión por"
                        + " garantía\",\"version\":5,\"actor\":\"falsificado\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.estado").value("EN_DIAGNOSTICO"))
        .andExpect(jsonPath("$.data.version").value(6));
    mvc.perform(get("/api/ordenes/" + id + "/historial").with(user("juan").roles("ASESOR")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalElements").value(7))
        .andExpect(jsonPath("$.data.content[0].actor").value("juan"))
        .andExpect(jsonPath("$.data.content[6].actor").value("supervisor"))
        .andExpect(jsonPath("$.data.content[6].motivo").value("Revisión por garantía"))
        .andExpect(jsonPath("$.data.content[6].anterior").value("CERRADA"));
  }

  @Test
  void transicionIlegalVersionObsoletaYMotivoVacioNoCreanHistorial() throws Exception {
    String id = crear();
    mvc.perform(
            patch("/api/ordenes/" + id + "/cambiar-estado")
                .with(user("juan").roles("ASESOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"CERRADA\",\"motivo\":\"Inválido\",\"version\":0}"))
        .andExpect(status().isConflict());
    mvc.perform(
            patch("/api/ordenes/" + id + "/reabrir")
                .with(user("juan").roles("ASESOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"Garantía\",\"version\":0}"))
        .andExpect(status().isConflict());
    mvc.perform(
            patch("/api/ordenes/" + id + "/cambiar-estado")
                .with(user("juan").roles("ASESOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"estado":"EN_DIAGNOSTICO","motivo":" ","version":0}
                    """))
        .andExpect(status().isBadRequest());
    cambiar(id, "EN_DIAGNOSTICO", 0);
    mvc.perform(
            patch("/api/ordenes/" + id + "/cambiar-estado")
                .with(user("juan").roles("ASESOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"EN_REPARACION\",\"motivo\":\"Avance\",\"version\":0}"))
        .andExpect(status().isConflict());
    assertThat(jdbc.queryForObject("select count(*) from historial_estado", Long.class))
        .isEqualTo(2);
  }

  @Test
  void falloDeAuditoriaRevierteRegistroYTransicion() throws Exception {
    String id = crear();
    doThrow(new IllegalStateException("fallo simulado interno")).when(historial).agregar(any());
    mvc.perform(
            patch("/api/ordenes/" + id + "/cambiar-estado")
                .with(user("juan").roles("ASESOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"EN_DIAGNOSTICO\",\"motivo\":\"Avance\",\"version\":0}"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.data.message").value("Error interno del servidor"));
    assertThat(
            jdbc.queryForObject("select estado from orden_servicio where id=?", String.class, id))
        .isEqualTo("REGISTRADA");
    mvc.perform(
            post("/api/ordenes/crear")
                .with(user("juan").roles("ASESOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
        .andExpect(status().isInternalServerError());
    assertThat(jdbc.queryForObject("select count(*) from orden_servicio", Long.class)).isEqualTo(1);
    assertThat(jdbc.queryForObject("select count(*) from historial_estado", Long.class))
        .isEqualTo(1);
  }

  @Test
  void dosCambiosConcurrentesConMismaVersionSoloUnoSeConfirma() throws Exception {
    String id = crear();
    var inicio = new CountDownLatch(1);
    try (var pool = Executors.newFixedThreadPool(2)) {
      Callable<Integer> cambio =
          () -> {
            inicio.await();
            return mvc.perform(
                    patch("/api/ordenes/" + id + "/cambiar-estado")
                        .with(user("juan").roles("ASESOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            "{\"estado\":\"EN_DIAGNOSTICO\",\"motivo\":\"Avance\",\"version\":0}"))
                .andReturn()
                .getResponse()
                .getStatus();
          };
      var a = pool.submit(cambio);
      var b = pool.submit(cambio);
      inicio.countDown();
      assertThat(List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS)))
          .containsExactlyInAnyOrder(200, 409);
    }
    assertThat(jdbc.queryForObject("select count(*) from historial_estado", Long.class))
        .isEqualTo(2);
  }

  @Test
  void seguridadMantieneContratoYSeparaRolInterno() throws Exception {
    mvc.perform(get("/api/ordenes/listar"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401));
    mvc.perform(get("/api/ordenes/listar").with(user("servicio").roles("SERVICIO")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403));
    String id = crear();
    mvc.perform(get("/internal/ordenes/" + id).with(user("juan").roles("ASESOR")))
        .andExpect(status().isForbidden());
    mvc.perform(get("/internal/ordenes/" + id).with(user("servicio").roles("SERVICIO")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.estado").value("REGISTRADA"));
  }

  @Test
  void cierreGuardaEventoEnLaMismaTransaccionYExigeRolTecnicoParaConfirmar() throws Exception {
    String id = crear();
    cambiar(id, "EN_DIAGNOSTICO", 0);
    cambiar(id, "EN_REPARACION", 1);
    cambiar(id, "REPARADA", 2);
    cambiar(id, "CERRADA", 3);
    var evento =
        cierres.pendientes().stream()
            .filter(c -> c.ordenId().toString().equals(id))
            .findFirst()
            .orElseThrow();
    assertThat(evento.version()).isEqualTo(4);
    mvc.perform(
            patch("/internal/ordenes/cierres/" + evento.id() + "/confirmar")
                .with(user("juan").roles("ASESOR")))
        .andExpect(status().isForbidden());
    mvc.perform(
            patch("/internal/ordenes/cierres/" + evento.id() + "/confirmar")
                .with(user("interno").roles("SERVICIO")))
        .andExpect(status().isOk());
    assertThat(cierres.pendientes()).noneMatch(c -> c.id().equals(evento.id()));
  }

  @Test
  void falloAlGuardarEventoRevierteCierreYSuHistorial() throws Exception {
    String id = crear();
    cambiar(id, "EN_DIAGNOSTICO", 0);
    cambiar(id, "EN_REPARACION", 1);
    cambiar(id, "REPARADA", 2);
    doThrow(new IllegalStateException("fallo del buzon")).when(cierres).registrar(any(), anyLong());
    mvc.perform(
            patch("/api/ordenes/" + id + "/cambiar-estado")
                .with(user("juan").roles("ASESOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"CERRADA\",\"motivo\":\"QA cierre\",\"version\":3}"))
        .andExpect(status().isInternalServerError());
    assertThat(
            jdbc.queryForObject("select estado from orden_servicio where id=?", String.class, id))
        .isEqualTo("REPARADA");
    assertThat(
            jdbc.queryForObject(
                "select count(*) from historial_estado where orden_id=?", Long.class, id))
        .isEqualTo(4);
  }
}
