package es.mjsplayground.acda.jdbc.almacen.ui;

import es.mjsplayground.acda.jdbc.almacen.model.entity.*;
import es.mjsplayground.acda.jdbc.almacen.model.service.*;
import java.math.BigDecimal;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class AlmacenView extends BorderPane {
  private final AlmacenService servicio;
  private final TableView<Producto> tabla = new TableView<>();
  private final TableView<Venta> historial = new TableView<>();
  private final Label mensaje = new Label();
  private final TextField nombre = new TextField(),
      precio = new TextField(),
      stock = new TextField();
  private final VBox formulario = new VBox(10);
  private long editando;

  public AlmacenView(AlmacenService servicio) {
    this.servicio = servicio;
    setPadding(new Insets(18));
    tabla.getColumns().add(col("Producto", Producto::nombre));
    tabla.getColumns().add(col("Precio (€)", p -> euros(p.precioCentimos())));
    tabla.getColumns().add(col("Existencias", p -> Integer.toString(p.stock())));
    tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabla.setPlaceholder(new Label("No hay productos. Usa Nuevo para añadir uno."));
    historial.getColumns().add(col("Producto", Venta::producto));
    historial.getColumns().add(col("Cantidad", v -> Integer.toString(v.cantidad())));
    historial.getColumns().add(col("Total (€)", v -> euros(v.totalCentimos())));
    historial.getColumns().add(col("Fecha (UTC)", Venta::fecha));
    historial.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    historial.setPlaceholder(new Label("Todavía no hay ventas."));
    Button nuevo = boton("Nuevo", () -> abrir(null));
    Button editar = boton("Editar", () -> abrir(tabla.getSelectionModel().getSelectedItem()));
    Button eliminar = boton("Eliminar", () -> eliminar());
    Button vender = boton("Vender", () -> vender());
    for (Button b : new Button[] {editar, eliminar, vender})
      b.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
    HBox acciones =
        new HBox(10, nuevo, editar, eliminar, vender, boton("Actualizar", this::cargar));
    VBox arriba = new VBox(12, new Label("Almacén y ventas"), acciones, mensaje);
    arriba.setPadding(new Insets(0, 0, 14, 0));
    setTop(arriba);
    Tab productos = new Tab("Productos", tabla), ventas = new Tab("Ventas", historial);
    productos.setClosable(false);
    ventas.setClosable(false);
    setCenter(new TabPane(productos, ventas));
    nombre.setPromptText("Nombre del producto");
    precio.setPromptText("25,90");
    stock.setPromptText("10");
    formulario
        .getChildren()
        .addAll(
            new Label("Producto"),
            etiqueta("Nombre", nombre),
            etiqueta("Precio (€)", precio),
            etiqueta("Existencias", stock),
            new HBox(8, boton("Guardar", this::guardar), boton("Cancelar", this::cerrar)));
    formulario.setPadding(new Insets(0, 0, 0, 18));
    formulario.setPrefWidth(270);
    mensaje.setWrapText(true);
    mensaje.setAccessibleRole(javafx.scene.AccessibleRole.TEXT);
  }

  private VBox etiqueta(String texto, TextField campo) {
    Label l = new Label(texto);
    l.setLabelFor(campo);
    return new VBox(4, l, campo);
  }

  private <T> TableColumn<T, String> col(String titulo, Function<T, String> f) {
    TableColumn<T, String> c = new TableColumn<>(titulo);
    c.setCellValueFactory(v -> new SimpleStringProperty(f.apply(v.getValue())));
    return c;
  }

  private Button boton(String texto, Runnable r) {
    Button b = new Button(texto);
    b.setOnAction(e -> probar(r));
    return b;
  }

  private void probar(Runnable r) {
    try {
      r.run();
    } catch (RuntimeException e) {
      mensaje.setText(e.getMessage());
      mensaje.getStyleClass().setAll("error");
    }
  }

  public void cargar() {
    probar(
        () -> {
          tabla.getItems().setAll(servicio.listar());
          historial.getItems().setAll(servicio.ventas());
        });
  }

  private void abrir(Producto p) {
    editando = p == null ? 0 : p.id();
    nombre.setText(p == null ? "" : p.nombre());
    precio.setText(p == null ? "" : euros(p.precioCentimos()));
    stock.setText(p == null ? "" : Integer.toString(p.stock()));
    setRight(formulario);
    nombre.requestFocus();
    mensaje.setText("");
  }

  private void cerrar() {
    setRight(null);
    editando = 0;
    nombre.clear();
    precio.clear();
    stock.clear();
    tabla.requestFocus();
  }

  private void guardar() {
    long centimos;
    int unidades;
    try {
      centimos =
          new BigDecimal(precio.getText().replace(',', '.')).movePointRight(2).longValueExact();
      unidades = Integer.parseInt(stock.getText());
    } catch (ArithmeticException | NumberFormatException e) {
      throw new ReglaNegocioException(
          "Escribe un precio con hasta dos decimales y existencias enteras.");
    }
    servicio.guardar(new Producto(editando, nombre.getText(), centimos, unidades));
    cerrar();
    cargar();
    ok("Producto guardado.");
  }

  private void eliminar() {
    Producto p = tabla.getSelectionModel().getSelectedItem();
    Alert a =
        new Alert(
            Alert.AlertType.CONFIRMATION,
            "¿Eliminar «" + p.nombre() + "»?",
            ButtonType.CANCEL,
            ButtonType.OK);
    a.initOwner(getScene().getWindow());
    a.setTitle("Eliminar producto");
    a.setHeaderText("Confirmar eliminación");
    a.getDialogPane().getStylesheets().add(getClass().getResource("/almacen.css").toExternalForm());
    ((Button) a.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
    ((Button) a.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
    if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
      servicio.eliminar(p.id());
      cerrar();
      cargar();
      ok("Producto eliminado.");
    }
  }

  private void vender() {
    Producto p = tabla.getSelectionModel().getSelectedItem();
    TextInputDialog d = new TextInputDialog("1");
    d.initOwner(getScene().getWindow());
    d.setTitle("Registrar venta");
    d.setHeaderText(p.nombre() + " · disponibles: " + p.stock());
    d.setContentText("Cantidad:");
    d.getDialogPane().getStylesheets().add(getClass().getResource("/almacen.css").toExternalForm());
    d.showAndWait()
        .ifPresent(
            texto -> {
              int cantidad;
              try {
                cantidad = Integer.parseInt(texto);
              } catch (NumberFormatException e) {
                throw new ReglaNegocioException("La cantidad debe ser un entero positivo.");
              }
              long total = servicio.vender(p.id(), cantidad);
              cargar();
              ok("Venta registrada: " + euros(total) + " €.");
            });
  }

  private void ok(String texto) {
    mensaje.getStyleClass().clear();
    mensaje.setText(texto);
  }

  private String euros(long centimos) {
    return BigDecimal.valueOf(centimos, 2).toPlainString();
  }
}
