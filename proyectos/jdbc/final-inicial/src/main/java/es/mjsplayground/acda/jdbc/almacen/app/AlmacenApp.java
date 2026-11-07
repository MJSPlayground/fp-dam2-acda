package es.mjsplayground.acda.jdbc.almacen.app;

import es.mjsplayground.acda.jdbc.almacen.config.Database;
import es.mjsplayground.acda.jdbc.almacen.model.service.AlmacenService;
import es.mjsplayground.acda.jdbc.almacen.model.service.OperacionesService;
import es.mjsplayground.acda.jdbc.almacen.ui.AlmacenView;
import es.mjsplayground.acda.jdbc.almacen.ui.OperacionesView;
import java.nio.file.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public final class AlmacenApp extends Application {
  @Override
  public void start(Stage stage) throws Exception {
    Path archivo = Path.of(System.getProperty("almacen.db", "datos/almacen.db")).toAbsolutePath();
    Files.createDirectories(archivo.getParent());
    Database db = new Database("jdbc:sqlite:" + archivo);
    db.preparar();
    AlmacenView view = new AlmacenView(new AlmacenService(db));
    OperacionesService operaciones = new OperacionesService(db);
    operaciones.preparar();
    Tab catalogo = new Tab("Catálogo y ventas", view);
    Tab operacionesTab = new Tab("Operaciones", new OperacionesView(operaciones));
    catalogo.setClosable(false);
    operacionesTab.setClosable(false);
    TabPane principal = new TabPane(catalogo, operacionesTab);
    principal
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (o, a, b) -> {
              if (b == catalogo) view.cargar();
            });
    Scene scene = new Scene(principal, 1100, 740);
    scene.getStylesheets().add(getClass().getResource("/almacen.css").toExternalForm());
    stage.getIcons().add(new Image(getClass().getResourceAsStream("/branding/mjs-playground.png")));
    stage.setTitle("Operaciones de almacén — MJS Playground");
    stage.setScene(scene);
    stage.setMinWidth(740);
    stage.setMinHeight(500);
    stage.show();
    view.cargar();
  }
}
