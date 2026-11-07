package es.mjsplayground.acda.jdbc.almacen;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.jdbc.almacen.config.Database;
import es.mjsplayground.acda.jdbc.almacen.model.service.AlmacenService;
import es.mjsplayground.acda.jdbc.almacen.ui.AlmacenView;
import java.nio.file.Path;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
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
