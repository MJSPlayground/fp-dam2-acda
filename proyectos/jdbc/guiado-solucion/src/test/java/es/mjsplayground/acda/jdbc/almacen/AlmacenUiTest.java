package es.mjsplayground.acda.jdbc.almacen;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.jdbc.almacen.config.Database;
import es.mjsplayground.acda.jdbc.almacen.model.entity.Producto;
import es.mjsplayground.acda.jdbc.almacen.model.entity.Venta;
import es.mjsplayground.acda.jdbc.almacen.model.service.AlmacenService;
import es.mjsplayground.acda.jdbc.almacen.ui.AlmacenView;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AlmacenUiTest {
  @TempDir Path temporal;

  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "qa.ui", matches = "true")
  @Test
  void altaGuardarCancelarYRedimensionar() throws Exception {
    CountDownLatch inicio = new CountDownLatch(1);
    Platform.startup(inicio::countDown);
    assertTrue(inicio.await(10, TimeUnit.SECONDS));
    Database db = new Database("jdbc:sqlite:" + temporal.resolve("ui.db"));
    db.preparar();
    FutureTask<Void> tarea =
        new FutureTask<>(
            () -> {
              Stage ventana = new Stage();
              try {
                AlmacenView vista = new AlmacenView(new AlmacenService(db));
                Scene scene = new Scene(vista, 980, 620);
                scene.getStylesheets().add(getClass().getResource("/almacen.css").toExternalForm());
                ventana.setScene(scene);
                ventana.show();
                vista.cargar();
                vista.applyCss();
                vista.layout();
                assertNull(vista.getRight());
                boton(vista, "Nuevo").fire();
                vista.applyCss();
                vista.layout();
                assertNotNull(vista.getRight());
                campo(vista, "Nombre del producto").setText("Producto UI");
                campo(vista, "25,90").setText("19,95");
                campo(vista, "10").setText("7");
                boton(vista, "Guardar").fire();
                assertNull(vista.getRight());
                assertEquals(4, new AlmacenService(db).listar().size());
                boton(vista, "Nuevo").fire();
                vista.applyCss();
                vista.layout();
                assertEquals("", campo(vista, "Nombre del producto").getText());
                campo(vista, "Nombre del producto").setText("Sin guardar");
                boton(vista, "Cancelar").fire();
                assertNull(vista.getRight());
                assertEquals(4, new AlmacenService(db).listar().size());
                @SuppressWarnings("unchecked")
                TableView<es.mjsplayground.acda.jdbc.almacen.model.entity.Producto> tabla =
                    (TableView<es.mjsplayground.acda.jdbc.almacen.model.entity.Producto>)
                        vista.lookupAll(".table-view").stream()
                            .filter(
                                n ->
                                    n instanceof TableView<?> t
                                        && !t.getItems().isEmpty()
                                        && t.getItems().getFirst()
                                            instanceof
                                            es.mjsplayground.acda.jdbc.almacen.model.entity
                                                .Producto)
                            .findFirst()
                            .orElseThrow();
                tabla.getSelectionModel().select(0);
                boton(vista, "Editar").fire();
                vista.applyCss();
                vista.layout();
                campo(vista, "Nombre del producto").setText("Editado UI");
                boton(vista, "Guardar").fire();
                assertNull(vista.getRight());
                assertTrue(
                    new AlmacenService(db)
                        .listar().stream().anyMatch(p -> p.nombre().equals("Editado UI")));
                boton(vista, "Nuevo").fire();
                vista.applyCss();
                vista.layout();
                campo(vista, "Nombre del producto").setText("Mal precio");
                campo(vista, "25,90").setText("1.999");
                campo(vista, "10").setText("3");
                boton(vista, "Guardar").fire();
                assertNotNull(vista.getRight());
                assertEquals(4, new AlmacenService(db).listar().size());
                boton(vista, "Cancelar").fire();
                // Los diálogos reales ejecutan un bucle JavaFX anidado; se atienden
                // desde la misma plataforma sin iniciar otro toolkit.
                AlmacenService servicio = new AlmacenService(db);
                Producto prescindible =
                    servicio.listar().stream()
                        .filter(p -> p.nombre().equals("Producto UI"))
                        .findFirst()
                        .orElseThrow();
                seleccionar(tabla, prescindible.id());
                dialogo(
                    vista,
                    "Eliminar",
                    pane -> {
                      Button cancelar = (Button) pane.lookupButton(ButtonType.CANCEL);
                      Button aceptar = (Button) pane.lookupButton(ButtonType.OK);
                      assertTrue(
                          cancelar.isDefaultButton(),
                          "Cancelar debe ser la opción segura predeterminada");
                      assertTrue(cancelar.isCancelButton(), "Escape debe asociarse a Cancelar");
                      assertFalse(aceptar.isDefaultButton());
                      cancelar.fire();
                    });
                assertEquals(4, servicio.listar().size());
                assertTrue(servicio.listar().stream().anyMatch(p -> p.id() == prescindible.id()));
                assertNull(vista.getRight());

                // Despachar Escape por la cadena de eventos real de JavaFX.
                seleccionar(tabla, prescindible.id());
                dialogo(
                    vista,
                    "Eliminar",
                    pane -> {
                      Window ventanaDialogo = pane.getScene().getWindow();
                      Event.fireEvent(
                          pane,
                          new KeyEvent(
                              KeyEvent.KEY_PRESSED,
                              "",
                              "",
                              KeyCode.ESCAPE,
                              false,
                              false,
                              false,
                              false));
                      assertFalse(
                          ventanaDialogo.isShowing(),
                          "Escape debe cerrar la confirmación sin eliminar");
                    });
                assertEquals(4, servicio.listar().size());

                seleccionar(tabla, prescindible.id());
                dialogo(
                    vista, "Eliminar", pane -> ((Button) pane.lookupButton(ButtonType.OK)).fire());
                assertEquals(3, servicio.listar().size());
                assertTrue(tabla.getItems().stream().noneMatch(p -> p.id() == prescindible.id()));
                assertNull(vista.getRight());

                Producto paraVenta =
                    servicio.listar().stream()
                        .filter(p -> p.stock() >= 2)
                        .findFirst()
                        .orElseThrow();
                seleccionar(tabla, paraVenta.id());
                dialogo(
                    vista,
                    "Vender",
                    pane -> {
                      TextField cantidad = (TextField) pane.lookup(".text-field");
                      assertNotNull(cantidad);
                      cantidad.setText("2");
                      ((Button) pane.lookupButton(ButtonType.OK)).fire();
                    });
                Producto vendido =
                    servicio.listar().stream()
                        .filter(p -> p.id() == paraVenta.id())
                        .findFirst()
                        .orElseThrow();
                assertEquals(paraVenta.stock() - 2, vendido.stock());
                assertEquals(
                    vendido.stock(),
                    tabla.getItems().stream()
                        .filter(p -> p.id() == vendido.id())
                        .findFirst()
                        .orElseThrow()
                        .stock());
                assertEquals(1, servicio.ventas().size());
                Venta venta = servicio.ventas().getFirst();
                assertEquals(2, venta.cantidad());
                assertEquals(paraVenta.precioCentimos() * 2, venta.totalCentimos());
                TabPane pestanas = (TabPane) vista.getCenter();
                @SuppressWarnings("unchecked")
                TableView<Venta> historial =
                    (TableView<Venta>) pestanas.getTabs().get(1).getContent();
                assertEquals(1, historial.getItems().size());
                assertEquals(venta, historial.getItems().getFirst());
                assertNull(vista.getRight());

                seleccionar(tabla, vendido.id());
                dialogo(
                    vista, "Eliminar", pane -> ((Button) pane.lookupButton(ButtonType.OK)).fire());
                assertEquals(3, servicio.listar().size());
                assertEquals(
                    vendido,
                    servicio.listar().stream()
                        .filter(p -> p.id() == vendido.id())
                        .findFirst()
                        .orElseThrow());
                assertEquals(List.of(venta), servicio.ventas());
                assertEquals(List.of(venta), historial.getItems());
                assertTrue(
                    ((javafx.scene.layout.VBox) vista.getTop())
                        .getChildren().stream()
                            .anyMatch(
                                n ->
                                    n instanceof Label l
                                        && l.getText()
                                            .contains("Conserva los productos que tienen ventas")));
                assertNull(vista.getRight());
                ventana.setWidth(760);
                ventana.setHeight(520);
                vista.applyCss();
                vista.layout();
                assertTrue(boton(vista, "Nuevo").isVisible());
                ventana.setWidth(980);
                ventana.setHeight(620);
                vista.applyCss();
                vista.layout();
                var imagen = scene.snapshot(null);
                var salida =
                    new java.awt.image.BufferedImage(
                        (int) imagen.getWidth(),
                        (int) imagen.getHeight(),
                        java.awt.image.BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < salida.getHeight(); y++)
                  for (int x = 0; x < salida.getWidth(); x++)
                    salida.setRGB(x, y, imagen.getPixelReader().getArgb(x, y));
                java.nio.file.Files.createDirectories(Path.of("target/qa"));
                javax.imageio.ImageIO.write(
                    salida, "png", Path.of("target/qa/almacen.png").toFile());
              } finally {
                ventana.close();
              }
              return null;
            });
    Platform.runLater(tarea);
    tarea.get(20, TimeUnit.SECONDS);
    Platform.exit();
  }

  void seleccionar(TableView<Producto> tabla, long id) {
    tabla
        .getSelectionModel()
        .select(tabla.getItems().stream().filter(p -> p.id() == id).findFirst().orElseThrow());
  }

  void dialogo(BorderPane vista, String accion, Consumer<DialogPane> respuesta) throws Exception {
    FutureTask<Void> atender =
        new FutureTask<>(
            () -> {
              DialogPane pane =
                  Window.getWindows().stream()
                      .filter(
                          w ->
                              w.isShowing()
                                  && w.getScene() != null
                                  && w.getScene().getRoot() instanceof DialogPane)
                      .map(w -> (DialogPane) w.getScene().getRoot())
                      .findFirst()
                      .orElseThrow();
              Window ventana = pane.getScene().getWindow();
              try {
                respuesta.accept(pane);
              } finally {
                if (ventana.isShowing()) ventana.hide();
              }
              return null;
            });
    Platform.runLater(atender);
    boton(vista, accion).fire();
    atender.get(5, TimeUnit.SECONDS);
  }

  Button boton(BorderPane raiz, String texto) {
    return raiz.lookupAll(".button").stream()
        .filter(n -> n instanceof Button b && b.getText().equals(texto))
        .map(n -> (Button) n)
        .findFirst()
        .orElseThrow();
  }

  TextField campo(BorderPane raiz, String texto) {
    return raiz.lookupAll(".text-field").stream()
        .filter(n -> n instanceof TextField f && f.getPromptText().equals(texto))
        .map(n -> (TextField) n)
        .findFirst()
        .orElseThrow();
  }
}
