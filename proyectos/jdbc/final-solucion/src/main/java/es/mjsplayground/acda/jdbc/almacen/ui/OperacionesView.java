package es.mjsplayground.acda.jdbc.almacen.ui;

import es.mjsplayground.acda.jdbc.almacen.model.entity.*;
import es.mjsplayground.acda.jdbc.almacen.model.service.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class OperacionesView extends BorderPane {
  private final OperacionesService servicio;
  private final Label mensaje = new Label();
  private final TableView<Producto> resultados = new TableView<>();
  private final TableView<Recepcion> historial = new TableView<>();
  private final TableView<LineaRecepcion> entrega = new TableView<>();
  private final TextField texto = new TextField(),
      maximo = new TextField("10"),
      id = new TextField("1"),
      porcentaje = new TextField("10"),
      cantidad = new TextField("1"),
      coste = new TextField("10,00"),
      presupuesto = new TextField("120,00");

  public OperacionesView(OperacionesService servicio) {
    this.servicio = servicio;
    setPadding(new Insets(14));
    presupuesto.setId("presupuesto");
    coste.setId("coste");
    cantidad.setId("cantidad");
    porcentaje.setId("porcentaje");
    id.setId("id");
    maximo.setId("maximo");
    texto.setId("texto");
    resultados.getColumns().add(col("Id", p -> Long.toString(p.id())));
    resultados.getColumns().add(col("Producto", Producto::nombre));
    resultados.getColumns().add(col("Precio (€)", p -> euros(p.precioCentimos())));
    resultados.getColumns().add(col("Existencias", p -> Integer.toString(p.stock())));
    resultados.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    resultados.setPlaceholder(new Label("Usa Buscar para consultar productos."));
    resultados
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (o, a, p) -> {
              if (p != null) id.setText(Long.toString(p.id()));
            });
    historial.getColumns().add(col("Producto", Recepcion::producto));
    historial.getColumns().add(col("Unidades", r -> Integer.toString(r.cantidad())));
    historial.getColumns().add(col("Total (€)", r -> euros(r.totalCentimos())));
    historial.getColumns().add(col("Fecha UTC", Recepcion::fecha));
    historial.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    historial.setPlaceholder(new Label("No hay recepciones registradas."));
    entrega.getColumns().add(col("Id producto", l -> Long.toString(l.productoId())));
    entrega.getColumns().add(col("Unidades", l -> Integer.toString(l.cantidad())));
    entrega.getColumns().add(col("Coste/u (€)", l -> euros(l.costeUnitarioCentimos())));
    entrega.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    entrega.setPrefHeight(140);
    entrega.setPlaceholder(new Label("Añade líneas para preparar una entrega conjunta."));
    VBox busqueda =
        new VBox(
            8,
            fila(
                campo("Parte del nombre", texto),
                campo("Máximo existencias", maximo),
                boton("Buscar", this::buscar)),
            resultados);
    VBox.setVgrow(resultados, Priority.ALWAYS);
    VBox edicion =
        new VBox(
            10,
            fila(
                campo("Id producto", id),
                campo("Variación %", porcentaje),
                boton(
                    "Revisar precio",
                    () -> {
                      long precio = servicio.revisarPrecio(numeroId(), decimal(porcentaje));
                      ok("Nuevo precio: " + euros(precio) + " €");
                    })),
            fila(
                campo("Unidades", cantidad),
                campo("Coste por unidad (€)", coste),
                boton(
                    "Recibir una línea",
                    () -> {
                      long total = servicio.recibirUna(linea());
                      actualizar();
                      ok("Recepción confirmada: " + euros(total) + " €");
                    })),
            new Label("Entrega conjunta"),
            fila(
                boton(
                    "Añadir a entrega",
                    () -> {
                      entrega.getItems().add(linea());
                      ok("Línea preparada; todavía no se ha guardado.");
                    }),
                boton(
                    "Quitar seleccionada",
                    () -> entrega.getItems().remove(entrega.getSelectionModel().getSelectedItem())),
                boton("Vaciar", () -> entrega.getItems().clear())),
            entrega,
            fila(
                campo("Presupuesto máximo (€)", presupuesto),
                boton(
                    "Confirmar entrega",
                    () -> {
                      long total =
                          servicio.recibir(
                              new ArrayList<>(entrega.getItems()), centimos(presupuesto));
                      entrega.getItems().clear();
                      actualizar();
                      ok("Entrega confirmada: " + euros(total) + " €");
                    })),
            new Label("Justificantes de recepción"),
            historial,
            boton("Actualizar justificantes", this::actualizar));
    VBox.setVgrow(historial, Priority.ALWAYS);
    ScrollPane scroll = new ScrollPane(edicion);
    scroll.setFitToWidth(true);
    historial.setPrefHeight(180);
    historial.setMinHeight(120);
    Tab consultar = new Tab("Localizar productos", busqueda),
        operar = new Tab("Precios y recepciones", scroll);
    consultar.setClosable(false);
    operar.setClosable(false);
    mensaje.setWrapText(true);
    mensaje.setAccessibleRole(javafx.scene.AccessibleRole.TEXT);
    setTop(new VBox(8, new Label("Operaciones de almacén"), mensaje));
    setCenter(new TabPane(consultar, operar));
  }

  private HBox fila(javafx.scene.Node... hijos) {
    HBox fila = new HBox(10, hijos);
    fila.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
    return fila;
  }

  private VBox campo(String nombre, TextField entrada) {
    entrada.setPrefColumnCount(12);
    Label l = new Label(nombre);
    l.setLabelFor(entrada);
    return new VBox(4, l, entrada);
  }

  private <T> TableColumn<T, String> col(String nombre, Function<T, String> f) {
    TableColumn<T, String> c = new TableColumn<>(nombre);
    c.setCellValueFactory(v -> new SimpleStringProperty(f.apply(v.getValue())));
    return c;
  }

  private Button boton(String nombre, Runnable accion) {
    Button b = new Button(nombre);
    b.setOnAction(
        e -> {
          try {
            accion.run();
          } catch (NumberFormatException | ArithmeticException ex) {
            error("Revisa los números: unidades enteras e importes con hasta dos decimales.");
          } catch (RuntimeException ex) {
            error(ex.getMessage());
          }
        });
    return b;
  }

  private long numeroId() {
    return Long.parseLong(id.getText().strip());
  }

  private BigDecimal decimal(TextField f) {
    return new BigDecimal(f.getText().strip().replace(',', '.'));
  }

  private long centimos(TextField f) {
    return decimal(f).movePointRight(2).longValueExact();
  }

  private LineaRecepcion linea() {
    return new LineaRecepcion(
        numeroId(), Integer.parseInt(cantidad.getText().strip()), centimos(coste));
  }

  private void buscar() {
    resultados
        .getItems()
        .setAll(servicio.buscar(texto.getText(), Integer.parseInt(maximo.getText().strip())));
    ok(resultados.getItems().size() + " productos encontrados.");
  }

  private void actualizar() {
    historial.getItems().setAll(servicio.historial());
  }

  private void ok(String texto) {
    mensaje.getStyleClass().clear();
    mensaje.setText(texto);
  }

  private void error(String texto) {
    mensaje.getStyleClass().setAll("error");
    mensaje.setText(texto);
  }

  private String euros(long valor) {
    return BigDecimal.valueOf(valor, 2).toPlainString();
  }
}
