package es.mjsplayground.acda.hibernate.biblioteca.ui;

import es.mjsplayground.acda.hibernate.biblioteca.model.service.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class BibliotecaView extends BorderPane {
  protected final BibliotecaService servicio;
  protected final TableView<LibroDatos> libros = new TableView<>();
  protected final TableView<BibliotecaService.Referencia> editoriales = new TableView<>(),
      autores = new TableView<>();
  protected final ComboBox<BibliotecaService.Referencia> filtroEditorial = new ComboBox<>();
  protected final TextField filtro = new TextField();
  protected final Label estado =
      new Label("Catálogo persistente. Configura MySQL y pulsa Actualizar.");
  protected final HBox accionesLibros = new HBox(8);

  public BibliotecaView(BibliotecaService servicio) {
    this.servicio = servicio;
    setPadding(new Insets(16));
    estado.setId("estado");
    estado.setWrapText(true);
    libros.setId("libros");
    libros.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    libros.getColumns().add(col("ISBN", LibroDatos::isbn));
    libros.getColumns().add(col("Título", LibroDatos::titulo));
    libros.getColumns().add(col("Editorial", LibroDatos::editorial));
    libros.getColumns().add(col("Autores", LibroDatos::autores));
    libros.getColumns().add(col("Precio (€)", l -> l.precio().toPlainString()));
    libros
        .getColumns()
        .add(
            col(
                "Detalle",
                l ->
                    l.idioma() == null
                        ? "Sin ficha"
                        : l.idioma() + " · " + l.paginas() + " págs."));
    libros.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    libros.setPlaceholder(
        new Label("No hay libros. Crea editoriales y autores antes del primer libro."));
    Button nuevo = boton("Nuevo libro", "nuevo-libro", () -> libro(null)),
        editar = boton("Editar libro", "editar-libro", () -> libro(seleccionado())),
        eliminar =
            boton(
                "Eliminar libro",
                "eliminar-libro",
                () -> {
                  LibroDatos l = seleccionado();
                  if (confirmar(
                      "Eliminar libro",
                      "¿Eliminar «" + l.titulo() + "» y su detalle? Los autores se conservan.")) {
                    servicio.eliminarLibro(l.id());
                    cargar();
                    mensaje("Libro eliminado; autores compartidos conservados.");
                  }
                });
    editar.disableProperty().bind(libros.getSelectionModel().selectedItemProperty().isNull());
    eliminar.disableProperty().bind(libros.getSelectionModel().selectedItemProperty().isNull());
    accionesLibros.getChildren().addAll(nuevo, editar, eliminar);
    filtro.setId("filtro-titulo");
    filtro.setPromptText("Buscar título (HQL parametrizado)");
    filtroEditorial.setId("filtro-editorial");
    filtroEditorial.setPromptText("Todas las editoriales");
    HBox filtros =
        new HBox(
            8,
            filtro,
            filtroEditorial,
            boton("Buscar", "buscar", this::buscar),
            boton(
                "Quitar filtros",
                "quitar-filtros",
                () -> {
                  filtro.clear();
                  filtroEditorial.setValue(null);
                  buscar();
                }));
    HBox.setHgrow(filtro, Priority.ALWAYS);
    TabPane pestañas = new TabPane();
    pestañas.getTabs().add(tab("Libros", new VBox(10, accionesLibros, filtros, libros)));
    VBox.setVgrow(libros, Priority.ALWAYS);
    pestañas.getTabs().add(tab("Editoriales", catalogo(editoriales, true)));
    pestañas.getTabs().add(tab("Autores", catalogo(autores, false)));
    setCenter(pestañas);
    HBox arriba =
        new HBox(
            10,
            new Label("Catálogo de biblioteca"),
            boton("Actualizar", "actualizar", this::cargar),
            boton(
                "Preparar demostración",
                "demo",
                () -> {
                  if (confirmar(
                      "Datos de demostración",
                      "Solo se crean en una base completamente vacía; no se sustituyen datos"
                          + " existentes. ¿Continuar?")) {
                    servicio.datosDemo();
                    cargar();
                    mensaje("Demostración creada explícitamente.");
                  }
                }));
    setTop(arriba);
    setBottom(estado);
    BorderPane.setMargin(arriba, new Insets(0, 0, 14, 0));
    BorderPane.setMargin(estado, new Insets(12, 0, 0, 0));
  }

  protected Tab tab(String titulo, javafx.scene.Node contenido) {
    Tab t = new Tab(titulo, contenido);
    t.setClosable(false);
    return t;
  }

  protected VBox catalogo(TableView<BibliotecaService.Referencia> tabla, boolean editorial) {
    String tipo = editorial ? "editorial" : "autor";
    tabla.setId(tipo + "es");
    tabla.getColumns().add(col("Nombre", BibliotecaService.Referencia::nombre));
    tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    Button editar =
        boton(
            "Editar",
            "editar-" + tipo,
            () -> nombreCatalogo(tabla.getSelectionModel().getSelectedItem(), editorial));
    Button eliminar =
        boton(
            "Eliminar",
            "eliminar-" + tipo,
            () -> {
              var r = tabla.getSelectionModel().getSelectedItem();
              if (confirmar(
                  "Eliminar " + tipo,
                  "¿Eliminar «"
                      + r.nombre()
                      + "»? Solo es posible si no tiene libros vinculados.")) {
                if (editorial) servicio.eliminarEditorial(r.id());
                else servicio.eliminarAutor(r.id());
                cargar();
                mensaje("Registro eliminado.");
              }
            });
    editar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
    eliminar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
    VBox v =
        new VBox(
            10,
            new HBox(
                8,
                boton("Nuevo", "nuevo-" + tipo, () -> nombreCatalogo(null, editorial)),
                editar,
                eliminar),
            tabla);
    VBox.setVgrow(tabla, Priority.ALWAYS);
    return v;
  }

  protected <T> TableColumn<T, String> col(String titulo, Function<T, String> f) {
    TableColumn<T, String> c = new TableColumn<>(titulo);
    c.setCellValueFactory(v -> new SimpleStringProperty(f.apply(v.getValue())));
    return c;
  }

  protected Button boton(String texto, String id, Runnable r) {
    Button b = new Button(texto);
    b.setId(id);
    b.setOnAction(e -> probar(r));
    return b;
  }

  protected void probar(Runnable r) {
    try {
      r.run();
    } catch (RuntimeException e) {
      estado.setText(e.getMessage());
      estado.getStyleClass().setAll("error");
    }
  }

  protected void mensaje(String s) {
    estado.getStyleClass().clear();
    estado.setText(s);
  }

  public void cargar() {
    probar(
        () -> {
          var es = servicio.editoriales();
          var as = servicio.autores();
          editoriales.getItems().setAll(es);
          autores.getItems().setAll(as);
          Long elegido =
              filtroEditorial.getValue() == null ? null : filtroEditorial.getValue().id();
          filtroEditorial.getItems().setAll(es);
          filtroEditorial.setValue(
              elegido == null
                  ? null
                  : es.stream().filter(e -> e.id() == elegido).findFirst().orElse(null));
          buscar();
          mensaje("Catálogo actualizado desde MySQL.");
        });
  }

  protected void buscar() {
    Long id = filtroEditorial.getValue() == null ? null : filtroEditorial.getValue().id();
    libros.getItems().setAll(servicio.listar(filtro.getText(), id));
  }

  protected LibroDatos seleccionado() {
    LibroDatos l = libros.getSelectionModel().getSelectedItem();
    if (l == null) throw new BibliotecaException("Selecciona un libro.");
    return l;
  }

  protected TextArea texto(String id, String valor) {
    TextArea t = new TextArea(valor == null ? "" : valor);
    t.setId(id);
    t.setPrefRowCount(2);
    t.setWrapText(true);
    return t;
  }

  protected VBox campo(String texto, Control c) {
    Label l = new Label(texto);
    l.setLabelFor(c);
    return new VBox(4, l, c);
  }

  protected void dialogo(String titulo, VBox contenido, Runnable guardar) {
    Dialog<ButtonType> d = new Dialog<>();
    d.setTitle(titulo);
    d.initOwner(getScene().getWindow());
    d.getDialogPane()
        .getStylesheets()
        .add(getClass().getResource("/biblioteca.css").toExternalForm());
    Label error = new Label();
    error.setWrapText(true);
    error.getStyleClass().add("error");
    error.setMinHeight(40);
    error.setMaxWidth(430);
    error.setId("error-dialogo");
    contenido.setPadding(new Insets(12));
    ScrollPane scroll = new ScrollPane(contenido);
    scroll.setFitToWidth(true);
    scroll.setPrefViewportHeight(440);
    scroll.setPrefViewportWidth(430);
    d.getDialogPane().setContent(new VBox(8, scroll, error));
    d.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
    Button ok = (Button) d.getDialogPane().lookupButton(ButtonType.OK);
    ok.addEventFilter(
        ActionEvent.ACTION,
        e -> {
          try {
            guardar.run();
            cargar();
            mensaje("Cambios guardados.");
          } catch (RuntimeException ex) {
            error.setText(ex.getMessage());
            e.consume();
          }
        });
    d.showAndWait();
  }

  protected boolean confirmar(String titulo, String mensaje) {
    Alert a = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.CANCEL, ButtonType.OK);
    a.initOwner(getScene().getWindow());
    a.setTitle(titulo);
    a.setHeaderText(titulo);
    a.getDialogPane()
        .getStylesheets()
        .add(getClass().getResource("/biblioteca.css").toExternalForm());
    ((Button) a.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
    ((Button) a.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
    return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
  }

  protected void nombreCatalogo(BibliotecaService.Referencia r, boolean editorial) {
    TextArea nombre = texto("nombre-catalogo", r == null ? "" : r.nombre());
    dialogo(
        editorial ? "Editorial" : "Autor",
        new VBox(10, campo("Nombre (1–80)", nombre)),
        () -> {
          if (editorial) servicio.guardarEditorial(r == null ? null : r.id(), nombre.getText());
          else servicio.guardarAutor(r == null ? null : r.id(), nombre.getText());
        });
  }

  protected void libro(LibroDatos actual) {
    TextField isbn = new TextField(actual == null ? "" : actual.isbn()),
        precio = new TextField(actual == null ? "" : actual.precio().toPlainString()),
        paginas =
            new TextField(
                actual == null || actual.paginas() == null ? "" : actual.paginas().toString());
    TextArea idioma = texto("idioma", actual == null ? "" : actual.idioma());
    isbn.setId("isbn");
    precio.setId("precio");
    paginas.setId("paginas");
    TextArea titulo = texto("titulo", actual == null ? "" : actual.titulo());
    ComboBox<BibliotecaService.Referencia> editorial = new ComboBox<>();
    editorial.setId("editorial-libro");
    editorial.getItems().setAll(servicio.editoriales());
    if (actual != null)
      editorial.getItems().stream()
          .filter(e -> e.id() == actual.editorialId())
          .findFirst()
          .ifPresent(editorial::setValue);
    ListView<BibliotecaService.Referencia> autores = new ListView<>();
    autores.setId("autores-libro");
    autores.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    autores.getItems().setAll(servicio.autores());
    autores.setPrefHeight(110);
    if (actual != null)
      for (int i = 0; i < autores.getItems().size(); i++)
        if (actual.autorIds().contains(autores.getItems().get(i).id()))
          autores.getSelectionModel().select(i);
    VBox campos =
        new VBox(
            10,
            campo("ISBN (13 dígitos)", isbn),
            campo("Título", titulo),
            campo("Precio (€)", precio),
            campo("Editorial", editorial),
            campo("Autores (1–5, Ctrl para seleccionar varios)", autores),
            campo("Idioma (vacío y páginas vacías: sin detalle)", idioma),
            campo("Páginas", paginas));
    dialogo(
        actual == null ? "Nuevo libro" : "Editar libro",
        campos,
        () -> {
          if (editorial.getValue() == null)
            throw new BibliotecaException("Selecciona una editorial.");
          BigDecimal p;
          Integer hojas;
          try {
            p = new BigDecimal(precio.getText().replace(',', '.'));
            hojas = paginas.getText().isBlank() ? null : Integer.valueOf(paginas.getText());
          } catch (NumberFormatException e) {
            throw new BibliotecaException("Precio numérico y páginas enteras.");
          }
          LibroSolicitud r =
              new LibroSolicitud(
                  isbn.getText(),
                  titulo.getText(),
                  p,
                  editorial.getValue().id(),
                  autores.getSelectionModel().getSelectedItems().stream()
                      .map(BibliotecaService.Referencia::id)
                      .toList(),
                  idioma.getText(),
                  hojas);
          servicio.guardarLibro(
              actual == null ? null : actual.id(), actual == null ? null : actual.version(), r);
        });
  }
}
