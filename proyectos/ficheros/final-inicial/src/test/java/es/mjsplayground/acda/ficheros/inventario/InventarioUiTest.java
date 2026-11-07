package es.mjsplayground.acda.ficheros.inventario;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.ficheros.inventario.model.entity.*;
import es.mjsplayground.acda.ficheros.inventario.model.repository.*;
import es.mjsplayground.acda.ficheros.inventario.model.service.*;
import es.mjsplayground.acda.ficheros.inventario.ui.InventarioView;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

@EnabledIfSystemProperty(named = "qa.ui", matches = "true")
class InventarioUiTest {
  @TempDir Path temporal;
  private static final boolean GUIADO_INICIAL = false;
  private static final boolean FINAL = true;
  private static final boolean FINAL_INICIAL = true;

  @Test
  void recorridoReal() throws Exception {
    CountDownLatch inicio = new CountDownLatch(1);
    try {
      Platform.startup(
          () -> {
            Platform.setImplicitExit(false);
            inicio.countDown();
          });
    } catch (IllegalStateException yaIniciado) {
      Platform.runLater(
          () -> {
            Platform.setImplicitExit(false);
            inicio.countDown();
          });
    }
    assertTrue(inicio.await(10, TimeUnit.SECONDS));
    FutureTask<Void> tarea =
        new FutureTask<>(
            () -> {
              Stage stage = new Stage();
              try {
                probar(stage);
              } finally {
                stage.close();
              }
              return null;
            });
    Platform.runLater(tarea);
    try {
      tarea.get(35, TimeUnit.SECONDS);
    } finally {
      Platform.exit();
    }
  }

  private void probar(Stage stage) throws Exception {
    Path carpeta = Files.createDirectory(temporal.resolve("inventario"));
    InventarioService service = new InventarioService();
    InventarioView vista = new InventarioView(service, carpeta);
    Scene scene = new Scene(vista, 1150, 700);
    scene.getStylesheets().add(getClass().getResource("/inventario.css").toExternalForm());
    stage.getIcons().add(new Image(getClass().getResourceAsStream("/branding/mjs-playground.png")));
    stage.setScene(scene);
    stage.setTitle("Inventario · verificación real");
    stage.show();
    vista.refrescar();
    vista.listarArchivos();
    layout(vista);
    TableView<Producto> tabla = tabla(vista);
    assertEquals(3, tabla.getItems().size());
    boton(vista, "nuevo").fire();
    layout(vista);
    texto(vista, "codigo").setText("UI");
    texto(vista, "nombre").setText("Producto UI");
    texto(vista, "unidades").setText("3");
    texto(vista, "precio").setText("4.50");
    if (GUIADO_INICIAL) {
      boton(vista, "guardar-producto").fire();
      assertEquals(3, service.listar().size());
      assertTrue(estado(vista).contains("Etapa pendiente"));
      boton(vista, "cancelar-producto").fire();
      boton(vista, "guardar").fire();
      assertFalse(Files.exists(carpeta.resolve("catalogo.csv")));
      assertTrue(estado(vista).contains("Etapa pendiente"));
      captura(scene, "inicial");
      return;
    }
    boton(vista, "guardar-producto").fire();
    assertEquals(4, service.listar().size());
    assertNull(((BorderPane) tabla.getUserData()).getRight());
    boton(vista, "nuevo").fire();
    layout(vista);
    assertEquals("", texto(vista, "nombre").getText());
    texto(vista, "nombre").setText("No guardar");
    boton(vista, "cancelar-producto").fire();
    assertEquals(4, service.listar().size());
    Path csv = carpeta.resolve("especial.csv");
    String nombre = "Ratón, \"óptico\"\nsegunda línea";
    Files.writeString(
        csv,
        "codigo,nombre,unidades,precio\r\n"
            + "ESPECIAL,\"Ratón, \"\"óptico\"\"\n"
            + "segunda línea\",4,12.34\r\n");
    vista.listarArchivos();
    seleccionar(vista, csv);
    confirmar(vista, "cargar", ButtonType.CANCEL);
    assertEquals(4, service.listar().size());
    confirmar(vista, "cargar", ButtonType.OK);
    assertEquals(1, service.listar().size());
    assertEquals(nombre, service.listar().getFirst().nombre());
    tabla.getSelectionModel().select(0);
    boton(vista, "editar").fire();
    layout(vista);
    assertEquals(nombre, texto(vista, "nombre").getText());
    captura(scene, "edicion-multilinea");
    texto(vista, "unidades").setText("9");
    texto(vista, "precio").setText("18.75");
    boton(vista, "guardar-producto").fire();
    assertEquals(nombre, service.listar().getFirst().nombre());
    assertEquals(9, service.listar().getFirst().unidades());
    assertNull(((BorderPane) tabla.getUserData()).getRight());
    tabla.getSelectionModel().select(0);
    boton(vista, "editar").fire();
    layout(vista);
    texto(vista, "unidades").setText("1.5");
    boton(vista, "guardar-producto").fire();
    assertEquals(9, service.listar().getFirst().unidades());
    assertNotNull(((BorderPane) tabla.getUserData()).getRight());
    boton(vista, "cancelar-producto").fire();
    for (Formato f : Formato.values()) {
      formato(vista).setValue(f);
      texto(vista, "destino").setText("salida." + f.name().toLowerCase(Locale.ROOT));
      boton(vista, "guardar").fire();
      Path salida = carpeta.resolve(texto(vista, "destino").getText());
      assertTrue(Files.isRegularFile(salida));
      InventarioService recuperado = new InventarioService();
      recuperado.cargar(salida, f);
      assertEquals(service.listar(), recuperado.listar());
    }
    List<Producto> antes = service.listar();
    Path roto = carpeta.resolve("roto.json");
    Files.writeString(roto, "[{\"codigo\":\"X\",\"nombre\":\"X\",\"unidades\":1.5,\"precio\":1}]");
    vista.listarArchivos();
    seleccionar(vista, roto);
    confirmar(vista, "cargar", ButtonType.OK);
    assertEquals(antes, service.listar());
    assertTrue(estado(vista).contains("No se pudo cargar"));
    texto(vista, "filtro").setText("INEXISTENTE");
    assertTrue(tabla.getItems().isEmpty());
    assertEquals(antes, service.listar());
    texto(vista, "filtro").clear();

    if (FINAL) {
      formato(vista).setValue(Formato.JSON);
      texto(vista, "destino").setText("seleccion.json");
      boton(vista, "exportar-filtrados").fire();
      if (FINAL_INICIAL) {
        assertFalse(Files.exists(carpeta.resolve("seleccion.json")));
        assertTrue(estado(vista).contains("Etapa pendiente"));
      } else {
        assertEquals(antes, new JsonProductoRepository().leer(carpeta.resolve("seleccion.json")));
      }
      Path lote = carpeta.resolve("lote-qa.csv");
      new CsvProductoRepository()
          .guardar(
              lote,
              List.of(
                  new Producto("ESPECIAL", nombre, 2, new BigDecimal("19.00")),
                  new Producto("NUEVO", "Nuevo", 3, new BigDecimal("5.00")),
                  new Producto("DUP", "Uno", 1, new BigDecimal("1.00")),
                  new Producto("DUP", "Dos", 2, new BigDecimal("2.00"))));
      vista.listarArchivos();
      seleccionar(vista, lote);
      boton(vista, "previsualizar").fire();
      assertEquals(antes, service.listar());
      if (FINAL_INICIAL) {
        assertTrue(estado(vista).contains("Etapa pendiente"));
      } else {
        assertTrue(texto(vista, "incidencias").getText().contains("Incidencias: 2"));
        confirmar(vista, "aplicar", ButtonType.CANCEL);
        assertEquals(antes, service.listar());
        confirmar(vista, "aplicar", ButtonType.OK);
        assertEquals(2, service.listar().size());
        assertEquals(
            11,
            service.listar().stream()
                .filter(p -> p.codigo().equals("ESPECIAL"))
                .findFirst()
                .orElseThrow()
                .unidades());
        confirmar(vista, "recuperar", ButtonType.OK);
        assertEquals(
            antes,
            service.listar(),
            "Recuperar tras aplicar debe usar el respaldo, aunque siga seleccionado el CSV del"
                + " lote");
        boton(vista, "informe").fire();
        assertTrue(Files.isRegularFile(carpeta.resolve("incidencias.csv")));
      }
    }

    tabla.getSelectionModel().select(0);
    confirmar(vista, "eliminar", ButtonType.CANCEL);
    assertEquals(1, service.listar().size());
    confirmar(vista, "eliminar", ButtonType.OK);
    assertTrue(service.listar().isEmpty());
    assertTrue(tabla.getItems().isEmpty());
    stage.setWidth(900);
    stage.setHeight(560);
    esperarRedimensionado();
    layout(vista);
    for (String id : List.of("nuevo", "guardar", "cargar")) {
      Button b = boton(vista, id);
      var bounds = b.localToScene(b.getBoundsInLocal());
      assertTrue(
          bounds.getMaxX() <= scene.getWidth() + 2 && bounds.getMaxY() <= scene.getHeight() + 2,
          "Acción recortada " + id + " " + bounds);
    }
    captura(scene, "compacta");
    boton(vista, "nuevo").fire();
    layout(vista);
    ScrollPane editor = (ScrollPane) vista.lookup("#editor-scroll");
    editor.setVvalue(1);
    esperarRedimensionado();
    layout(vista);
    for (String id : List.of("guardar", "cargar", "guardar-producto", "cancelar-producto")) {
      Button action = boton(vista, id);
      var bounds = action.localToScene(action.getBoundsInLocal());
      assertTrue(
          bounds.getMaxY() <= scene.getHeight() + 2 && bounds.getMinY() >= 0,
          "Acción recortada con editor abierto: " + id + " " + bounds);
    }
    captura(scene, "compacta-formulario");
    boton(vista, "cancelar-producto").fire();
    stage.setWidth(1150);
    stage.setHeight(700);
    esperarRedimensionado();
    layout(vista);
    captura(scene, "normal");
  }

  private void esperarRedimensionado() {
    Object clave = new Object();
    javafx.animation.PauseTransition espera =
        new javafx.animation.PauseTransition(javafx.util.Duration.millis(250));
    espera.setOnFinished(e -> Platform.exitNestedEventLoop(clave, null));
    espera.play();
    Platform.enterNestedEventLoop(clave);
  }

  private void layout(InventarioView vista) {
    vista.applyCss();
    vista.layout();
  }

  private Button boton(InventarioView vista, String id) {
    return (Button) vista.lookup("#" + id);
  }

  private TextInputControl texto(InventarioView vista, String id) {
    return (TextInputControl) vista.lookup("#" + id);
  }

  @SuppressWarnings("unchecked")
  private TableView<Producto> tabla(InventarioView vista) {
    return (TableView<Producto>) vista.lookup("#tabla-productos");
  }

  @SuppressWarnings("unchecked")
  private ComboBox<Formato> formato(InventarioView vista) {
    return (ComboBox<Formato>) vista.lookup("#formato");
  }

  @SuppressWarnings("unchecked")
  private void seleccionar(InventarioView vista, Path archivo) {
    ((ListView<Path>) vista.lookup("#archivos")).getSelectionModel().select(archivo);
  }

  private String estado(InventarioView vista) {
    return ((Label) vista.lookup("#estado")).getText();
  }

  private void confirmar(InventarioView vista, String id, ButtonType respuesta) {
    Platform.runLater(
        () -> {
          DialogPane pane =
              (DialogPane)
                  Window.getWindows().stream()
                      .filter(
                          w ->
                              w.isShowing()
                                  && w.getScene() != null
                                  && w.getScene().getRoot() instanceof DialogPane)
                      .findFirst()
                      .orElseThrow()
                      .getScene()
                      .getRoot();
          assertTrue(((Button) pane.lookupButton(ButtonType.CANCEL)).isDefaultButton());
          ((Button) pane.lookupButton(respuesta)).fire();
        });
    boton(vista, id).fire();
  }

  private void captura(Scene scene, String nombre) throws Exception {
    var imagen = scene.snapshot(null);
    var salida =
        new java.awt.image.BufferedImage(
            (int) imagen.getWidth(),
            (int) imagen.getHeight(),
            java.awt.image.BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < salida.getHeight(); y++)
      for (int x = 0; x < salida.getWidth(); x++)
        salida.setRGB(x, y, imagen.getPixelReader().getArgb(x, y));
    Files.createDirectories(Path.of("target/qa"));
    javax.imageio.ImageIO.write(
        salida, "png", Path.of("target/qa/inventario-" + nombre + ".png").toFile());
  }
}
