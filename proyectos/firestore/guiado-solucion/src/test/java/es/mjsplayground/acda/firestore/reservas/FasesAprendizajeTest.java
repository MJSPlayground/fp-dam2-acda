package es.mjsplayground.acda.firestore.reservas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import es.mjsplayground.acda.firestore.reservas.api.*;
import es.mjsplayground.acda.firestore.reservas.model.entity.*;
import es.mjsplayground.acda.firestore.reservas.model.repository.*;
import es.mjsplayground.acda.firestore.reservas.model.service.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Ejecutar solo la fase alcanzada: VM options -Daprendizaje.fase=conversion, etc. */
class FasesAprendizajeTest {
  Reserva muestra() {
    return new Reserva(
        null,
        "SALA-A",
        "2044-02-15",
        10,
        12,
        "Coordinación",
        8,
        List.of("Proyector, HD"),
        Map.of("actividad", "Taller", "contacto", "Recepción"),
        "ACTIVA",
        "");
  }

  ReservaRequest solicitud(int asistentes) {
    return new ReservaRequest(
        "SALA-A",
        "2044-02-15",
        10,
        12,
        "Coordinación",
        asistentes,
        List.of("Proyector, HD"),
        Map.of("actividad", "Taller", "contacto", "Recepción"));
  }

  FirestoreReservaRepository sinRed() {
    Firestore db = mock(Firestore.class);
    when(db.collection("reservas")).thenReturn(mock(CollectionReference.class));
    return new FirestoreReservaRepository(db);
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "conversion|todas")
  void conversionConservaTiposListaMapaEIdentidad() {
    var repo = sinRed();
    var r = muestra();
    Map<String, Object> campos = repo.datos(r);
    assertEquals(
        Set.of(
            "espacio",
            "fecha",
            "inicio",
            "fin",
            "responsable",
            "asistentes",
            "equipo",
            "detalles",
            "estado",
            "motivoCancelacion"),
        campos.keySet());
    assertEquals(r.equipo(), campos.get("equipo"));
    assertEquals(r.detalles(), campos.get("detalles"));
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.getId()).thenReturn("fase-conversion");
    for (String campo : List.of("espacio", "fecha", "responsable", "estado", "motivoCancelacion"))
      when(doc.getString(campo)).thenReturn((String) campos.get(campo));
    for (String campo : List.of("inicio", "fin", "asistentes"))
      when(doc.get(campo)).thenReturn(((Number) campos.get(campo)).longValue());
    when(doc.get("equipo")).thenReturn(campos.get("equipo"));
    when(doc.get("detalles")).thenReturn(campos.get("detalles"));
    assertEquals(repo.conId(r, "fase-conversion"), repo.mapear(doc));
    when(doc.get("inicio")).thenReturn(Long.MAX_VALUE);
    assertNotNull(assertThrows(PersistenciaException.class, () -> repo.mapear(doc)).getCause());
    when(doc.get("inicio")).thenReturn(10L);
    when(doc.get("equipo")).thenReturn(List.of(7L));
    assertNotNull(assertThrows(PersistenciaException.class, () -> repo.mapear(doc)).getCause());
  }

  Firestore local(String proyecto) {
    // Siempre emulador y proyecto demo propio; nunca usa credenciales cloud.
    return FirestoreOptions.newBuilder()
        .setProjectId(proyecto)
        .setEmulatorHost(System.getenv().getOrDefault("FIRESTORE_EMULATOR_HOST", "127.0.0.1:8085"))
        .setCredentials(
            GoogleCredentials.create(new AccessToken("solo-emulador", new Date(Long.MAX_VALUE))))
        .build()
        .getService();
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "alta-lectura|todas")
  void crearYLeerSinNecesitarActualizarNiEliminar() throws Exception {
    try (Firestore db = local("demo-fase-" + UUID.randomUUID().toString().substring(0, 8))) {
      var repo = new FirestoreReservaRepository(db);
      try {
        Reserva creada = repo.crear(muestra());
        assertNotNull(creada.id());
        assertEquals(creada, repo.obtener(creada.id()).orElseThrow());
        assertTrue(repo.obtener("ausente").isEmpty());
      } finally {
        for (var doc : db.collection("reservas").get().get().getDocuments())
          doc.getReference().delete().get();
      }
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "repositorio|todas")
  void crudRealSinNecesitarServicioNiControlador() throws Exception {
    try (Firestore db = local("demo-fase-" + UUID.randomUUID().toString().substring(0, 8))) {
      var repo = new FirestoreReservaRepository(db);
      String id = null;
      try {
        Reserva r = repo.crear(muestra());
        id = r.id();
        assertNotNull(id);
        assertEquals(r, repo.obtener(id).orElseThrow());
        Reserva cambio =
            new Reserva(
                id,
                r.espacio(),
                r.fecha(),
                r.inicio(),
                r.fin(),
                "Recepción",
                9,
                r.equipo(),
                r.detalles(),
                r.estado(),
                r.motivoCancelacion());
        repo.actualizar(id, cambio);
        assertEquals(cambio, repo.obtener(id).orElseThrow());
        db.collection("reservas").document(id).update("inicio", 9.5).get();
        String corrupto = id;
        assertNotNull(
            assertThrows(PersistenciaException.class, () -> repo.obtener(corrupto)).getCause());
        repo.eliminar(id);
        assertTrue(repo.obtener(id).isEmpty());
        id = null;
      } finally {
        if (id != null) repo.eliminar(id);
      }
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "consultas|todas")
  void filtrosRealesNoDevuelvenFilasQueNoCumplen() throws Exception {
    try (Firestore db = local("demo-fase-" + UUID.randomUUID().toString().substring(0, 8))) {
      var repo = new FirestoreReservaRepository(db);
      List<String> ids = new ArrayList<>();
      try {
        Reserva a = repo.crear(muestra());
        ids.add(a.id());
        Reserva b =
            repo.crear(
                new Reserva(
                    null, "SALA-B", a.fecha(), 10, 12, "Otra", 3, List.of(), Map.of(), "ACTIVA",
                    ""));
        ids.add(b.id());
        assertEquals(List.of(a), repo.listar("SALA-A", a.fecha(), 8));
        assertTrue(repo.listar("SALA-A", a.fecha(), 9).isEmpty());
        assertEquals(2, repo.listar(null, null, null).size());
      } finally {
        ids.forEach(repo::eliminar);
      }
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "servicio|todas")
  void servicioValidaAntesDePersistirSinNecesitarHTTP() {
    var repo = mock(FirestoreReservaRepository.class);
    var service = new ReservaService(repo);
    when(repo.crear(any())).thenAnswer(i -> sinRed().conId(i.getArgument(0), "fase-servicio"));
    Reserva creada = service.crear(solicitud(8));
    assertEquals("ACTIVA", creada.estado());
    assertEquals("fase-servicio", creada.id());
    clearInvocations(repo);
    assertEquals(
        400, assertThrows(ReservaException.class, () -> service.crear(solicitud(13))).estado());
    verifyNoInteractions(repo);
    when(repo.obtener("ausente")).thenReturn(Optional.empty());
    assertEquals(
        404,
        assertThrows(ReservaException.class, () -> service.actualizar("ausente", solicitud(8)))
            .estado());
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "rest|todas")
  void controladorConstruye201Location200Y204SinNecesitarFirestore() throws Exception {
    var service = mock(ReservaService.class);
    Reserva creada = sinRed().conId(muestra(), "fase-rest");
    when(service.crear(any())).thenReturn(creada);
    when(service.actualizar(eq("fase-rest"), any())).thenReturn(creada);
    var api =
        MockMvcBuilders.standaloneSetup(new ReservaController(service))
            .setControllerAdvice(new ApiErrors())
            .build();
    String json =
        "{\"espacio\":\"SALA-A\",\"fecha\":\"2044-02-15\",\"inicio\":10,\"fin\":12,\"responsable\":\"Coordinación\",\"asistentes\":8,\"equipo\":[],\"detalles\":{}}";
    api.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/reservas/fase-rest"))
        .andExpect(jsonPath("$.id").value("fase-rest"));
    api.perform(
            put("/api/reservas/fase-rest").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isOk());
    api.perform(delete("/api/reservas/fase-rest"))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));
    verify(service).eliminar("fase-rest");
    when(service.obtener("fallo"))
        .thenThrow(
            new PersistenciaException(
                "mensaje privado", new IllegalStateException("causa preparada")));
    api.perform(get("/api/reservas/fallo"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.estado").value(500))
        .andExpect(
            jsonPath("$.mensaje")
                .value("No se pudo completar la operación. Consulta el registro del servidor."));
  }
}
