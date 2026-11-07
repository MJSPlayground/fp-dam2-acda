package es.mjsplayground.acda.hibernate.biblioteca.app;

import es.mjsplayground.acda.hibernate.biblioteca.config.HibernateFactory;
import es.mjsplayground.acda.hibernate.biblioteca.model.service.BibliotecaService;
import es.mjsplayground.acda.hibernate.biblioteca.ui.BibliotecaView;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.stage.*;

public final class BibliotecaApp extends Application {
  private final HibernateFactory factory = new HibernateFactory();

  @Override
  public void start(Stage stage) {
    BibliotecaView view = new BibliotecaView(new BibliotecaService(factory));
    Scene scene = new Scene(view, 1150, 700);
    scene.getStylesheets().add(getClass().getResource("/biblioteca.css").toExternalForm());
    stage.getIcons().add(new Image(getClass().getResourceAsStream("/branding/mjs-playground.png")));
    stage.setTitle("Biblioteca · MJS Playground");
    stage.setScene(scene);
    var area = Screen.getPrimary().getVisualBounds();
    stage.setWidth(Math.min(1150, area.getWidth() * .94));
    stage.setHeight(Math.min(700, area.getHeight() * .88));
    stage.setMinWidth(900);
    stage.setMinHeight(560);
    stage.show();
    view.cargar();
  }

  @Override
  public void stop() {
    factory.close();
  }
}
