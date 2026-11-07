package es.mjsplayground.acda.jdbc.almacen;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.jdbc.almacen.config.Database;
import es.mjsplayground.acda.jdbc.almacen.model.service.*;
import es.mjsplayground.acda.jdbc.almacen.ui.OperacionesView;
import java.nio.file.Path;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OperacionesUiTest {
  @TempDir Path temporal;

  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "qa.ui", matches = "true")
  @Test
  void recorridoOperaciones() throws Exception {
    CountDownLatch inicio = new CountDownLatch(1);
    Platform.startup(inicio::countDown);
    assertTrue(inicio.await(10, TimeUnit.SECONDS));
    Database db = new Database("jdbc:sqlite:" + temporal.resolve("ui.db"));
    db.preparar();
    OperacionesService servicio = new OperacionesService(db);
    servicio.preparar();
    FutureTask<Void> tarea =
        new FutureTask<>(
            () -> {
              Stage ventana = new Stage();
              try {
                OperacionesView vista = new OperacionesView(servicio);
                Scene scene = new Scene(vista, 1080, 700);
                scene.getStylesheets().add(getClass().getResource("/almacen.css").toExternalForm());
                ventana.setScene(scene);
                ventana.show();
                vista.applyCss();
                vista.layout();
                boton(vista, "Buscar").fire();
                assertEquals(2, servicio.buscar("", 10).size());
                TabPane tabs = (TabPane) vista.getCenter();
                tabs.getSelectionModel().select(1);
                vista.applyCss();
                vista.layout();
                campo(vista, "id").setText("1");
                campo(vista, "porcentaje").setText("10");
                boton(vista, "Revisar precio").fire();
                assertEquals(
                    2849,
                    new AlmacenService(db)
                        .listar().stream()
                            .filter(p -> p.id() == 1)
                            .findFirst()
                            .orElseThrow()
                            .precioCentimos());
                campo(vista, "cantidad").setText("2");
                campo(vista, "coste").setText("10,00");
                boton(vista, "Añadir a entrega").fire();
                campo(vista, "id").setText("3");
                campo(vista, "cantidad").setText("1");
                campo(vista, "coste").setText("100,00");
                boton(vista, "Añadir a entrega").fire();
                campo(vista, "presupuesto").setText("119,99");
                boton(vista, "Confirmar entrega").fire();
                assertTrue(servicio.historial().isEmpty());
                TableView<?> entrega =
                    vista.lookupAll(".table-view").stream()
                        .filter(
                            n ->
                                n instanceof TableView<?> t
                                    && !t.getItems().isEmpty()
                                    && t.getItems().getFirst()
                                        instanceof
                                        es.mjsplayground.acda.jdbc.almacen.model.entity
                                            .LineaRecepcion)
                        .map(n -> (TableView<?>) n)
                        .findFirst()
                        .orElseThrow();
                assertEquals(2, entrega.getItems().size());
                campo(vista, "presupuesto").setText("120,00");
                boton(vista, "Confirmar entrega").fire();
                assertEquals(2, servicio.historial().size());
                assertTrue(entrega.getItems().isEmpty());
                campo(vista, "cantidad").setText("0");
                boton(vista, "Recibir una línea").fire();
                assertEquals(2, servicio.historial().size());
                ventana.setWidth(760);
                ventana.setHeight(520);
                vista.applyCss();
                vista.layout();
                assertNotNull(vista.lookup(".scroll-pane"));
                ventana.setWidth(1100);
                ventana.setHeight(740);
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
                    salida, "png", Path.of("target/qa/operaciones.png").toFile());
              } finally {
                ventana.close();
              }
              return null;
            });
    Platform.runLater(tarea);
    try {
      tarea.get(25, TimeUnit.SECONDS);
    } finally {
      Platform.exit();
    }
  }

  Button boton(OperacionesView raiz, String texto) {
    return raiz.lookupAll(".button").stream()
        .filter(n -> n instanceof Button b && b.getText().equals(texto))
        .map(n -> (Button) n)
        .findFirst()
        .orElseThrow();
  }

  TextField campo(OperacionesView raiz, String id) {
    return (TextField) raiz.lookup("#" + id);
  }
}
