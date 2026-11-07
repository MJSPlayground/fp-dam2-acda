package es.mjsplayground.acda.ficheros.inventario.app;

import es.mjsplayground.acda.ficheros.inventario.model.service.InventarioService;
import es.mjsplayground.acda.ficheros.inventario.ui.InventarioView;
import java.nio.file.*;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.stage.*;

public final class InventarioApp extends Application {
  @Override
  public void start(Stage stage) throws Exception {
    Path carpeta =
        Path.of(System.getProperty("inventario.dir", "datos/inventario")).toAbsolutePath();
    Files.createDirectories(carpeta);
    InventarioView vista = new InventarioView(new InventarioService(), carpeta);
    Scene scene = new Scene(vista, 1150, 700);
    scene.getStylesheets().add(getClass().getResource("/inventario.css").toExternalForm());
    stage.getIcons().add(new Image(getClass().getResourceAsStream("/branding/mjs-playground.png")));
    stage.setTitle("Inventario local · MJS Playground");
    stage.setScene(scene);
    var area = Screen.getPrimary().getVisualBounds();
    stage.setWidth(Math.min(1150, area.getWidth() * .94));
    stage.setHeight(Math.min(700, area.getHeight() * .88));
    stage.setMinWidth(900);
    stage.setMinHeight(560);
    stage.show();
    vista.refrescar();
    vista.listarArchivos();
  }
}
