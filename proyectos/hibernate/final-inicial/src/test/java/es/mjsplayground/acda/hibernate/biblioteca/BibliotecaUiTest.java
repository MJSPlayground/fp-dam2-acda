package es.mjsplayground.acda.hibernate.biblioteca;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.hibernate.biblioteca.config.HibernateFactory;
import es.mjsplayground.acda.hibernate.biblioteca.model.service.*;
import es.mjsplayground.acda.hibernate.biblioteca.ui.BibliotecaView;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "qa.ui", matches = "true")
class BibliotecaUiTest {
  static final boolean INICIAL = true, FINAL = true;

  @Test
  void interfazConMysqlReal() throws Exception {
    String schema = "qa_hibernate_ui_" + UUID.randomUUID().toString().replace("-", "");
    String admin =
        System.getenv()
            .getOrDefault(
                "QA_MYSQL_ADMIN_URL",
                "jdbc:mysql://127.0.0.1:3308/mysql?sslMode=DISABLED&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    String user = System.getenv().getOrDefault("QA_MYSQL_ADMIN_USER", "root"),
        pass = System.getenv().getOrDefault("QA_MYSQL_ADMIN_PASSWORD", "qa_root_local");
    try (Connection c = DriverManager.getConnection(admin, user, pass);
        Statement s = c.createStatement()) {
      s.executeUpdate("CREATE DATABASE " + schema + " CHARACTER SET utf8mb4");
    }
    String url =
        admin.substring(0, admin.lastIndexOf('/') + 1)
            + schema
            + "?sslMode=DISABLED&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    try (SessionFactory factory = HibernateFactory.crear(url, user, pass, "update")) {
      BibliotecaService service = new BibliotecaService(() -> factory);
      CountDownLatch ready = new CountDownLatch(1);
      try {
        Platform.startup(
            () -> {
              Platform.setImplicitExit(false);
              ready.countDown();
            });
      } catch (IllegalStateException e) {
        Platform.runLater(
            () -> {
              Platform.setImplicitExit(false);
              ready.countDown();
            });
      }
      assertTrue(ready.await(10, TimeUnit.SECONDS));
      FutureTask<Void> task =
          new FutureTask<>(
              () -> {
                Stage stage = new Stage();
                try {
                  runUi(stage, service);
                } finally {
                  stage.close();
                }
                return null;
              });
      Platform.runLater(task);
      try {
        task.get(90, TimeUnit.SECONDS);
      } finally {
        Platform.exit();
      }
    } finally {
      assertTrue(schema.matches("qa_hibernate_ui_[a-f0-9]{32}"));
      try (Connection c = DriverManager.getConnection(admin, user, pass);
          Statement s = c.createStatement()) {
        s.executeUpdate("DROP DATABASE " + schema);
      }
    }
  }

  void runUi(Stage stage, BibliotecaService service) throws Exception {
    BibliotecaView view = new BibliotecaView(service);
    Scene scene = new Scene(view, 1180, 740);
    scene.getStylesheets().add(getClass().getResource("/biblioteca.css").toExternalForm());
    stage.setScene(scene);
    stage.setTitle("Biblioteca · QA real");
    stage.show();
    view.cargar();
    layout(view);
    if (INICIAL && !FINAL) {
      assertTrue(((Label) view.lookup("#estado")).getText().contains("Etapa pendiente"));
      dialog(view, "demo", p -> button(p, ButtonType.OK).fire());
      assertTrue(((Label) view.lookup("#estado")).getText().contains("Etapa pendiente"));
      snapshot(scene, "starter");
      resize(stage, view, scene);
      return;
    }
    dialog(view, "demo", p -> button(p, ButtonType.OK).fire());
    assertEquals(2, service.listar("", null).size());
    TableView<LibroDatos> table = table(view);
    assertEquals(2, table.getItems().size());
    dialog(view, "nuevo-libro", p -> button(p, ButtonType.CANCEL).fire());
    assertEquals(2, table.getItems().size());
    String title = "QA, \"datos\"\nsegunda línea", lang = "Español\nlocal";
    dialog(
        view,
        "nuevo-libro",
        p -> {
          text(p, "isbn").setText("9780000000999");
          text(p, "titulo").setText(title);
          text(p, "precio").setText("mal");
          combo(p, "editorial-libro").getSelectionModel().selectFirst();
          ((ListView<?>) p.lookup("#autores-libro")).getSelectionModel().selectFirst();
          button(p, ButtonType.OK).fire();
          assertTrue(p.getScene().getWindow().isShowing());
          layout(p);
          Label visibleError = (Label) p.lookup("#error-dialogo");
          assertFalse(visibleError.getText().isBlank());
          assertTrue(
              visibleError.localToScene(visibleError.getBoundsInLocal()).getMaxY()
                  < p.getScene().getHeight());
          try {
            snapshot(p.getScene(), "dialogo-error");
          } catch (Exception e) {
            throw new AssertionError(e);
          }
          text(p, "precio").setText("12.50");
          text(p, "idioma").setText(lang);
          text(p, "paginas").setText("123");
          button(p, ButtonType.OK).fire();
        });
    assertEquals(3, service.listar("", null).size());
    LibroDatos created = service.listar("QA", null).getFirst();
    assertEquals(title, created.titulo());
    assertEquals(lang, created.idioma());
    select(table, created.id());
    dialog(
        view,
        "editar-libro",
        p -> {
          assertEquals(title, text(p, "titulo").getText());
          assertEquals(lang, text(p, "idioma").getText());
          text(p, "precio").setText("13.00");
          button(p, ButtonType.OK).fire();
        });
    assertEquals(title, service.obtener(created.id()).titulo());
    assertEquals(lang, service.obtener(created.id()).idioma());
    text(view, "filtro-titulo").setText("' or 1=1 --");
    ((Button) view.lookup("#buscar")).fire();
    assertTrue(table.getItems().isEmpty());
    ((Button) view.lookup("#quitar-filtros")).fire();
    if (FINAL) {
      select(table, created.id());
      dialog(
          view,
          "duplicar",
          p -> {
            text(p, "nuevo-isbn").setText("9780000000888");
            text(p, "nuevo-titulo").setText("Copia UI");
            button(p, ButtonType.OK).fire();
            if (p.getScene().getWindow().isShowing()) button(p, ButtonType.CANCEL).fire();
          });
      if (INICIAL) {
        assertEquals(3, service.listar("", null).size());
      } else {
        assertEquals(4, service.listar("", null).size());
        LibroDatos copy = service.listar("Copia UI", null).getFirst();
        assertNotEquals(created.detalleId(), copy.detalleId());
      }
    }
    snapshot(scene, "normal");
    resize(stage, view, scene);
    select(table, created.id());
    dialog(
        view,
        "eliminar-libro",
        p -> {
          assertTrue(button(p, ButtonType.CANCEL).isDefaultButton());
          button(p, ButtonType.CANCEL).fire();
        });
    assertNotNull(service.obtener(created.id()));
    dialog(view, "eliminar-libro", p -> button(p, ButtonType.OK).fire());
    assertThrows(BibliotecaException.class, () -> service.obtener(created.id()));
    assertEquals(2, service.autores().size());
  }

  void resize(Stage stage, Parent view, Scene scene) throws Exception {
    stage.setWidth(920);
    stage.setHeight(580);
    Object key = new Object();
    javafx.animation.PauseTransition wait =
        new javafx.animation.PauseTransition(javafx.util.Duration.millis(250));
    wait.setOnFinished(e -> Platform.exitNestedEventLoop(key, null));
    wait.play();
    Platform.enterNestedEventLoop(key);
    layout(view);
    assertTrue(scene.getWidth() < 1000);
    snapshot(scene, "compacta");
  }

  void dialog(Parent view, String id, Consumer<DialogPane> action) {
    java.util.concurrent.atomic.AtomicReference<Throwable> error =
        new java.util.concurrent.atomic.AtomicReference<>();
    Platform.runLater(
        () -> {
          DialogPane p =
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
          try {
            action.accept(p);
          } catch (Throwable e) {
            error.set(e);
            button(p, ButtonType.CANCEL).fire();
          }
        });
    ((Button) view.lookup("#" + id)).fire();
    if (error.get() != null) throw new AssertionError(error.get());
    layout(view);
  }

  Button button(DialogPane p, ButtonType t) {
    return (Button) p.lookupButton(t);
  }

  TextInputControl text(Parent p, String id) {
    return (TextInputControl) p.lookup("#" + id);
  }

  ComboBox<?> combo(Parent p, String id) {
    return (ComboBox<?>) p.lookup("#" + id);
  }

  @SuppressWarnings("unchecked")
  TableView<LibroDatos> table(Parent p) {
    return (TableView<LibroDatos>) p.lookup("#libros");
  }

  void select(TableView<LibroDatos> t, long id) {
    t.getSelectionModel().clearSelection();
    t.getItems().stream()
        .filter(l -> l.id() == id)
        .findFirst()
        .ifPresent(t.getSelectionModel()::select);
  }

  void layout(Parent p) {
    p.applyCss();
    p.layout();
  }

  void snapshot(Scene scene, String name) throws Exception {
    var image = scene.snapshot(null);
    var out =
        new java.awt.image.BufferedImage(
            (int) image.getWidth(),
            (int) image.getHeight(),
            java.awt.image.BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < out.getHeight(); y++)
      for (int x = 0; x < out.getWidth(); x++)
        out.setRGB(x, y, image.getPixelReader().getArgb(x, y));
    Files.createDirectories(Path.of("target/qa"));
    javax.imageio.ImageIO.write(
        out, "png", Path.of("target/qa/biblioteca-" + name + ".png").toFile());
  }
}
