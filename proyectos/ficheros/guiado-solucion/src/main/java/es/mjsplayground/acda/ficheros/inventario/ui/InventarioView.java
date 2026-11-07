package es.mjsplayground.acda.ficheros.inventario.ui;

import es.mjsplayground.acda.ficheros.inventario.model.entity.*;
import es.mjsplayground.acda.ficheros.inventario.model.service.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;

public class InventarioView extends BorderPane {
  protected final InventarioService service;
  protected final TextField carpeta = new TextField(),
      destino = new TextField("catalogo.csv"),
      filtro = new TextField();
  protected final ComboBox<Formato> formato = new ComboBox<>();
  protected final ListView<Path> archivos = new ListView<>();
  protected final TableView<Producto> tabla = new TableView<>();
  protected final Label estado =
      new Label("Catálogo de demostración en memoria; guarda para persistir.");
  protected final VBox formulario = new VBox(9);
  protected final ScrollPane editorScroll = new ScrollPane(formulario);
  protected final TextField codigo = new TextField(),
      unidades = new TextField(),
      precio = new TextField();
  protected final TextArea nombre = new TextArea();
  protected String editando;
  protected final VBox zonaInferior = new VBox(8);

  public InventarioView(InventarioService service, Path inicial) {
    this.service = service;
    setPadding(new Insets(12));
    carpeta.setId("carpeta");
    carpeta.setText(inicial.toAbsolutePath().toString());
    carpeta.setAccessibleText("Directorio de trabajo");
    HBox.setHgrow(carpeta, Priority.ALWAYS);
    Button explorar =
        boton(
            "Examinar…",
            "examinar",
            () -> {
              DirectoryChooser c = new DirectoryChooser();
              c.setTitle("Ubicación de inventario");
              var dir = c.showDialog(getScene().getWindow());
              if (dir != null) {
                carpeta.setText(dir.toString());
                listarArchivos();
              }
            });
    ComboBox<String> extension = new ComboBox<>();
    extension.setId("extension");
    extension.getItems().addAll("Todos", "csv", "json", "bin");
    extension.setValue("Todos");
    extension.setOnAction(e -> listarArchivos());
    Button listar = boton("Usar carpeta", "listar", this::listarArchivos);
    Button ejemplos =
        boton(
            "Copiar ejemplos",
            "ejemplos",
            () -> {
              service.copiarEjemplos(directorio());
              listarArchivos();
              aviso("Ejemplos copiados desde el classpath; los existentes no se sobrescriben.");
            });
    HBox ubicacion = new HBox(8, new Label("Ubicación"), carpeta, explorar, listar);
    archivos.setId("archivos");
    archivos.setCellFactory(
        v ->
            new ListCell<>() {
              @Override
              protected void updateItem(Path p, boolean vacia) {
                super.updateItem(p, vacia);
                setText(vacia || p == null ? null : p.getFileName().toString());
              }
            });
    archivos
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (o, a, p) -> {
              if (p != null) {
                String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
                for (Formato f : Formato.values())
                  if (n.endsWith("." + f.name().toLowerCase(Locale.ROOT))) formato.setValue(f);
              }
            });
    VBox lateral =
        new VBox(8, new Label("Archivos de esta carpeta"), extension, archivos, ejemplos);
    lateral.setPrefWidth(230);
    VBox.setVgrow(archivos, Priority.ALWAYS);
    tabla.setId("tabla-productos");
    tabla.getColumns().add(col("Código", Producto::codigo));
    tabla.getColumns().add(col("Producto", Producto::nombre));
    tabla.getColumns().add(col("Stock", p -> Integer.toString(p.unidades())));
    tabla.getColumns().add(col("Precio (€)", p -> p.precio().toPlainString()));
    tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabla.setPlaceholder(new Label("No hay productos para este filtro"));
    Button nuevo = boton("Nuevo", "nuevo", () -> abrir(null)),
        editar =
            boton("Editar", "editar", () -> abrir(tabla.getSelectionModel().getSelectedItem())),
        eliminar = boton("Eliminar", "eliminar", this::eliminar);
    editar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
    eliminar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
    filtro.setId("filtro");
    filtro.setPromptText("Buscar por código o nombre");
    filtro.textProperty().addListener((o, a, b) -> probar(this::refrescar));
    HBox.setHgrow(filtro, Priority.ALWAYS);
    HBox acciones = new HBox(8, nuevo, editar, eliminar, filtro);
    BorderPane contenido = new BorderPane(tabla);
    VBox catalogo = new VBox(10, acciones, contenido);
    VBox.setVgrow(contenido, Priority.ALWAYS);
    nombre.setPrefRowCount(2);
    nombre.setWrapText(true);
    formulario.getStyleClass().add("formulario");
    editorScroll.setId("editor-scroll");
    editorScroll.setFitToWidth(true);
    editorScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    editorScroll.setPrefWidth(245);
    editorScroll.setMinHeight(0);
    formulario.setPrefWidth(230);
    codigo.setId("codigo");
    nombre.setId("nombre");
    unidades.setId("unidades");
    precio.setId("precio");
    formulario
        .getChildren()
        .addAll(
            new Label("Producto"),
            etiqueta("Código", codigo),
            etiqueta("Nombre", nombre),
            etiqueta("Unidades", unidades),
            etiqueta("Precio (€)", precio),
            boton(
                "Guardar producto",
                "guardar-producto",
                () -> {
                  Producto p;
                  try {
                    p =
                        new Producto(
                            codigo.getText(),
                            nombre.getText(),
                            Integer.parseInt(unidades.getText()),
                            new BigDecimal(precio.getText().replace(',', '.')));
                  } catch (NumberFormatException ex) {
                    throw new InventarioException(
                        "Unidades enteras y precio numérico con hasta dos decimales.");
                  }
                  service.guardarProducto(editando, p);
                  cerrar();
                  refrescar();
                  aviso("Producto actualizado en memoria; guarda el catálogo para persistir.");
                }),
            boton("Cancelar", "cancelar-producto", this::cerrar));
    HBox centro = new HBox(12, lateral, catalogo);
    HBox.setHgrow(catalogo, Priority.ALWAYS);
    setCenter(centro);
    // Formulario contextual sin reservar espacio cuando está cerrado.
    tabla.setUserData(contenido);
    formato.setId("formato");
    formato.getItems().addAll(Formato.values());
    formato.setValue(Formato.CSV);
    formato
        .valueProperty()
        .addListener(
            (o, a, b) -> {
              if (b != null) destino.setText("catalogo." + b.name().toLowerCase(Locale.ROOT));
            });
    destino.setId("destino");
    destino.setAccessibleText("Nombre del archivo de destino");
    HBox persistencia =
        new HBox(
            8,
            new Label("Formato"),
            formato,
            new Label("Guardar como"),
            destino,
            boton("Cargar seleccionado", "cargar", this::cargar),
            boton("Guardar catálogo", "guardar", this::guardar));
    estado.setId("estado");
    estado.setWrapText(true);
    zonaInferior.getChildren().addAll(persistencia, estado);
    setBottom(zonaInferior);
    setTop(
        new VBox(
            10,
            new Label("Inventario local de productos"),
            ubicacion,
            new Label(
                "Los cambios permanecen en memoria hasta guardar. BIN: solo archivos propios.")));
    BorderPane.setMargin(getTop(), new Insets(0, 0, 12, 0));
    BorderPane.setMargin(zonaInferior, new Insets(12, 0, 0, 0));
  }

  protected VBox etiqueta(String texto, Control campo) {
    Label l = new Label(texto);
    l.setLabelFor(campo);
    return new VBox(4, l, campo);
  }

  protected <T> TableColumn<T, String> col(String titulo, Function<T, String> f) {
    TableColumn<T, String> c = new TableColumn<>(titulo);
    c.setCellValueFactory(v -> new SimpleStringProperty(f.apply(v.getValue())));
    return c;
  }

  protected Button boton(String texto, String id, Runnable accion) {
    Button b = new Button(texto);
    b.setId(id);
    b.setOnAction(e -> probar(accion));
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

  protected void aviso(String texto) {
    estado.getStyleClass().clear();
    estado.setText(texto);
  }

  protected Path directorio() {
    return Path.of(carpeta.getText()).toAbsolutePath().normalize();
  }

  protected Path seleccionado() {
    Path p = archivos.getSelectionModel().getSelectedItem();
    if (p == null) throw new InventarioException("Selecciona un archivo de la lista.");
    return p;
  }

  protected Path destino() {
    Path nombre = Path.of(destino.getText());
    if (nombre.isAbsolute()
        || nombre.getRoot() != null
        || nombre.getNameCount() != 1
        || destino.getText().isBlank()
        || destino.getText().equals(".")
        || destino.getText().equals(".."))
      throw new InventarioException("Escribe solo un nombre de archivo en la carpeta elegida.");
    return directorio().resolve(nombre);
  }

  public void listarArchivos() {
    probar(
        () -> {
          ComboBox<?> ext = (ComboBox<?>) lookup("#extension");
          String extension =
              ext == null || "Todos".equals(ext.getValue()) ? "" : String.valueOf(ext.getValue());
          archivos.getItems().setAll(service.archivos(directorio(), extension));
        });
  }

  public void refrescar() {
    tabla.getItems().setAll(service.filtrar(filtro.getText(), 0));
  }

  protected void abrir(Producto p) {
    editando = p == null ? null : p.codigo();
    codigo.setText(p == null ? "" : p.codigo());
    codigo.setDisable(p != null);
    nombre.setText(p == null ? "" : p.nombre());
    unidades.setText(p == null ? "0" : Integer.toString(p.unidades()));
    precio.setText(p == null ? "" : p.precio().toPlainString());
    ((BorderPane) tabla.getUserData()).setRight(editorScroll);
    if (p == null) codigo.requestFocus();
    else nombre.requestFocus();
  }

  protected void cerrar() {
    ((BorderPane) tabla.getUserData()).setRight(null);
    editando = null;
    codigo.clear();
    nombre.clear();
    unidades.clear();
    precio.clear();
    tabla.requestFocus();
  }

  protected boolean confirmar(String titulo, String texto) {
    Alert a = new Alert(Alert.AlertType.CONFIRMATION, texto, ButtonType.CANCEL, ButtonType.OK);
    a.initOwner(getScene().getWindow());
    a.setTitle(titulo);
    a.setHeaderText(titulo);
    a.getDialogPane()
        .getStylesheets()
        .add(getClass().getResource("/inventario.css").toExternalForm());
    ((Button) a.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
    ((Button) a.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
    return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
  }

  protected void eliminar() {
    Producto p = tabla.getSelectionModel().getSelectedItem();
    if (confirmar("Eliminar producto", "¿Eliminar «" + p.nombre() + "» del catálogo en memoria?")) {
      service.eliminar(p.codigo());
      cerrar();
      refrescar();
      aviso("Producto eliminado en memoria; guarda para persistir.");
    }
  }

  protected void cargar() {
    Path p = seleccionado();
    if (confirmar(
        "Sustituir catálogo",
        "Se sustituirá todo el catálogo en memoria, incluidos cambios no guardados. ¿Cargar "
            + p.getFileName()
            + "?")) {
      service.cargar(p, formato.getValue());
      cerrar();
      refrescar();
      aviso("Catálogo cargado desde " + p);
    }
  }

  protected void guardar() {
    Path p = destino();
    if (Files.exists(p)
        && !confirmar("Sobrescribir archivo", "¿Reemplazar " + p.getFileName() + "?")) return;
    service.guardar(p, formato.getValue());
    listarArchivos();
    aviso("Catálogo guardado en " + p);
  }
}
