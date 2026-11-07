package es.mjsplayground.acda.jdbc.almacen.app;

import es.mjsplayground.acda.jdbc.almacen.config.Database;
import es.mjsplayground.acda.jdbc.almacen.model.service.AlmacenService;
import es.mjsplayground.acda.jdbc.almacen.ui.AlmacenView;
import java.nio.file.*;
import javafx.application.Application;
import javafx.scene.Scene;
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
    Scene scene = new Scene(view, 980, 620);
    scene.getStylesheets().add(getClass().getResource("/almacen.css").toExternalForm());
    stage.getIcons().add(new Image(getClass().getResourceAsStream("/branding/mjs-playground.png")));
    stage.setTitle("Almacén y ventas · MJS Playground");
    stage.setScene(scene);
    stage.setMinWidth(740);
    stage.setMinHeight(500);
    stage.show();
    view.cargar();
  }
}
