package es.mjsplayground.acda.ficheros.gestor;

import es.mjsplayground.acda.ficheros.model.entity.Cliente;
import es.mjsplayground.acda.ficheros.model.entity.Operacion;
import es.mjsplayground.acda.ficheros.model.entity.Producto;
import es.mjsplayground.acda.ficheros.model.dao.ProductoFormatos;
import es.mjsplayground.acda.ficheros.model.dao.InformeOperacionesDao;
import es.mjsplayground.acda.ficheros.model.service.ClienteService;
import es.mjsplayground.acda.ficheros.model.service.ComercialService;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class GestorApp extends Application {
    private ComercialService service;
    private final ClienteService formatos = new ClienteService();
    private final ProductoFormatos formatosProducto = new ProductoFormatos();
    private final TableView<Object> tabla = new TableView<>();
    private final ComboBox<String> seccion = new ComboBox<>();
    private final TextField busqueda = new TextField();
    private final Label estado = new Label();
    private final Button editar = new Button("Editar"), eliminar = new Button("Eliminar");
    private Stage stage;

    @Override public void start(Stage stage) throws Exception {
        this.stage = stage;
        Path archivo = getParameters().getRaw().isEmpty()
                ? Path.of(System.getProperty("user.home"), "MJSPlayground", "comercial.json")
                : Path.of(getParameters().getRaw().getFirst());
        service = new ComercialService(archivo);
        Image icono = icono();
        if (icono != null) stage.getIcons().add(icono);

        Label marca = new Label("MJS Playground"); marca.getStyleClass().add("marca");
        HBox cabecera = new HBox(10);
        if (icono != null) { ImageView imagen = new ImageView(icono); imagen.setFitWidth(28); imagen.setFitHeight(28); cabecera.getChildren().add(imagen); }
        cabecera.getChildren().add(marca); cabecera.getStyleClass().add("cabecera");

        seccion.getItems().addAll("Clientes", "Productos", "Operaciones"); seccion.getSelectionModel().selectFirst();
        seccion.setOnAction(e -> refrescar());
        busqueda.setPromptText("Buscar en la tabla"); busqueda.textProperty().addListener((o, a, b) -> refrescar());
        HBox.setHgrow(busqueda, Priority.ALWAYS);
        Button nuevo = new Button("Nuevo"), importar = new Button("Importar"), exportar = new Button("Exportar");
        nuevo.getStyleClass().add("principal"); nuevo.setOnAction(e -> formularioNuevo());
        editar.setOnAction(e -> formularioEditar()); eliminar.setOnAction(e -> eliminar());
        importar.setOnAction(e -> importar()); exportar.setOnAction(e -> exportar());
        HBox herramientas = new HBox(8, seccion, busqueda, nuevo, editar, eliminar, importar, exportar);
        herramientas.getStyleClass().add("herramientas");
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPlaceholder(new Label("No hay registros para esta búsqueda"));
        tabla.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> actualizarAcciones());
        estado.getStyleClass().add("estado");
        BorderPane root = new BorderPane();
        root.setTop(new VBox(cabecera, herramientas)); root.setCenter(tabla); root.setBottom(estado);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/gestor.css").toExternalForm());
        stage.setScene(scene); stage.setTitle("Gestor comercial · MJS Playground");
        Rectangle2D visual = Screen.getPrimary().getVisualBounds();
        stage.setWidth(Math.min(1080, visual.getWidth() * .88));
        stage.setHeight(Math.min(700, visual.getHeight() * .84));
        stage.setMinWidth(700); stage.setMinHeight(460);
        refrescar(); stage.show();
    }
    private Image icono() {
        try (InputStream in = getClass().getResourceAsStream("/branding/mjs-playground.png")) {
            return in == null ? null : new Image(in);
        } catch (Exception e) { return null; }
    }
    private <T> void columna(String titulo, Function<T, String> valor) {
        TableColumn<Object, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(c -> new ReadOnlyStringWrapper(valor.apply((T) c.getValue())));
        tabla.getColumns().add(col);
    }
    private void refrescar() {
        String tipo = seccion.getValue(), filtro = busqueda.getText().toLowerCase();
        tabla.getColumns().clear();
        List<?> datos;
        switch (tipo) {
            case "Clientes" -> {
                columna("ID", (Cliente c) -> c.id()); columna("Nombre", (Cliente c) -> c.nombre());
                columna("Ciudad", (Cliente c) -> c.ciudad()); columna("Correo", (Cliente c) -> c.correo());
                datos = service.estado().clientes();
            }
            case "Productos" -> {
                columna("Código", (Producto p) -> p.codigo()); columna("Nombre", (Producto p) -> p.nombre());
                columna("Unidades", (Producto p) -> Integer.toString(p.unidades()));
                columna("Precio", (Producto p) -> p.precio().toPlainString());
                datos = service.estado().productos();
            }
            default -> {
                columna("ID", (Operacion o) -> o.id()); columna("Cliente", (Operacion o) -> o.clienteId());
                columna("Productos", (Operacion o) -> o.lineas().stream()
                        .map(l -> l.productoCodigo() + " × " + l.cantidad()).reduce((a, b) -> a + ", " + b).orElse(""));
                columna("Importe", (Operacion o) -> o.importe().toPlainString());
                datos = service.estado().operaciones();
            }
        }
        tabla.setItems(FXCollections.observableArrayList(datos.stream()
                .filter(o -> o.toString().toLowerCase().contains(filtro)).map(o -> (Object) o).toList()));
        estado.setText(tipo + ": " + tabla.getItems().size() + " visibles · Datos externos guardados en el perfil de usuario");
        actualizarAcciones();
    }
    private void actualizarAcciones() {
        boolean seleccion = tabla.getSelectionModel().getSelectedItem() != null;
        boolean operacion = seccion.getValue().equals("Operaciones");
        editar.setDisable(!seleccion || operacion); eliminar.setDisable(!seleccion || operacion);
    }
    private void formularioNuevo() { formulario(null); }
    private void formularioEditar() { formulario(tabla.getSelectionModel().getSelectedItem()); }
    private void formulario(Object actual) {
        String tipo = seccion.getValue();
        if (tipo.equals("Operaciones") && actual != null) return;
        String[] etiquetas = tipo.equals("Clientes") ? new String[]{"ID", "Nombre", "Ciudad", "Correo"}
                : tipo.equals("Productos") ? new String[]{"Código", "Nombre", "Unidades", "Precio"}
                : new String[]{"ID", "Cliente ID", "Productos (código:cantidad)"};
        String[] valores = new String[etiquetas.length];
        if (actual instanceof Cliente c) valores = new String[]{c.id(), c.nombre(), c.ciudad(), c.correo()};
        if (actual instanceof Producto p) valores = new String[]{p.codigo(), p.nombre(), String.valueOf(p.unidades()), p.precio().toPlainString()};
        TextField[] campos = new TextField[etiquetas.length]; GridPane grid = new GridPane(); grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(18));
        for (int i = 0; i < etiquetas.length; i++) {
            campos[i] = new TextField(valores[i] == null ? "" : valores[i]);
            campos[i].setPromptText(etiquetas[i]);
            grid.add(new Label(etiquetas[i]), 0, i); grid.add(campos[i], 1, i);
        }
        if (actual != null) campos[0].setDisable(true);
        Dialog<ButtonType> dialogo = new Dialog<>(); dialogo.initOwner(stage);
        String singular = tipo.equals("Clientes") ? "cliente" : tipo.equals("Productos") ? "producto" : "operación";
        dialogo.setTitle(actual == null ? "Nuevo " + singular : "Editar " + singular);
        dialogo.getDialogPane().setContent(grid); dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialogo.getDialogPane().getStylesheets().add(getClass().getResource("/gestor.css").toExternalForm());
        dialogo.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, evento -> {
          try {
            if (tipo.equals("Clientes")) service.guardarCliente(new Cliente(campos[0].getText(), campos[1].getText(), campos[2].getText(), campos[3].getText()));
            else if (tipo.equals("Productos")) service.guardarProducto(new Producto(campos[0].getText(), campos[1].getText(),
                    Integer.parseInt(campos[2].getText()), new BigDecimal(campos[3].getText())));
            else {
                Map<String, Integer> cantidades = new LinkedHashMap<>();
                for (String parte : campos[2].getText().split(",")) {
                    String[] par = parte.trim().split(":", -1);
                    if (par.length != 2 || par[0].isBlank() || cantidades.putIfAbsent(par[0].trim(), Integer.parseInt(par[1].trim())) != null)
                        throw new IllegalArgumentException("Usa código:cantidad sin productos repetidos");
                }
                service.registrar(campos[0].getText(), campos[1].getText(), cantidades);
            }
            refrescar();
          } catch (Exception ex) { evento.consume(); error("No se pudo guardar", ex); }
        });
        dialogo.showAndWait();
    }
    private void eliminar() {
        Object elegido = tabla.getSelectionModel().getSelectedItem(); if (elegido == null) return;
        ButtonType confirmar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar el registro seleccionado?", ButtonType.CANCEL, confirmar);
        alerta.initOwner(stage); alerta.setTitle("Confirmar eliminación"); alerta.setHeaderText("Esta acción modifica los datos guardados");
        alerta.getDialogPane().getStylesheets().add(getClass().getResource("/gestor.css").toExternalForm());
        if (alerta.showAndWait().orElse(ButtonType.CANCEL) != confirmar) return;
        try {
            if (elegido instanceof Cliente c) service.eliminarCliente(c.id());
            if (elegido instanceof Producto p) service.eliminarProducto(p.codigo());
            refrescar();
        } catch (Exception ex) { error("No se pudo eliminar", ex); }
    }
    private void importar() {
        FileChooser chooser = new FileChooser(); chooser.setTitle("Importar " + seccion.getValue().toLowerCase() + " CSV, XML o JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Datos", "*.csv", "*.xml", "*.json"));
        var elegido = chooser.showOpenDialog(stage); if (elegido == null) return;
        try {
            String nombre = elegido.getName(), formato = nombre.substring(nombre.lastIndexOf('.') + 1);
            int aceptados = 0, duplicados = 0;
            List<String> errores = new ArrayList<>();
            if (seccion.getValue().equals("Clientes")) {
                var resultado = formatos.importar(formato, elegido.toPath());
                resultado.incidencias().forEach(i -> errores.add(i.toString()));
                for (Cliente c : resultado.clientes()) {
                    if (service.estado().clientes().stream().anyMatch(x -> x.id().equals(c.id()))) { duplicados++; continue; }
                    try { service.guardarCliente(c); aceptados++; }
                    catch (Exception ex) { errores.add(c.id() + ": " + ex.getMessage()); }
                }
            } else if (seccion.getValue().equals("Productos")) {
                var resultado = formatosProducto.leer(elegido.toPath(), formato);
                errores.addAll(resultado.incidencias());
                for (Producto p : resultado.productos()) {
                    if (service.estado().productos().stream().anyMatch(x -> x.codigo().equals(p.codigo()))) { duplicados++; continue; }
                    try { service.guardarProducto(p); aceptados++; }
                    catch (Exception ex) { errores.add(p.codigo() + ": " + ex.getMessage()); }
                }
            } else {
                throw new IllegalArgumentException("Importa primero clientes y productos; las operaciones se registran en la aplicación");
            }
            refrescar(); mensaje("Importación", "Aceptados: " + aceptados
                    + ", duplicados: " + duplicados + ", incidencias: " + errores.size()
                    + (errores.isEmpty() ? "" : "\n" + String.join("\n", errores.stream().limit(8).toList())));
        } catch (Exception ex) { error("No se pudo importar", ex); }
    }
    private void exportar() {
        FileChooser chooser = new FileChooser(); chooser.setTitle("Exportar " + seccion.getValue().toLowerCase());
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV", "*.csv"),
                new FileChooser.ExtensionFilter("XML", "*.xml"), new FileChooser.ExtensionFilter("JSON", "*.json"));
        var elegido = chooser.showSaveDialog(stage); if (elegido == null) return;
        try {
            String nombre = elegido.getName(), formato = nombre.substring(nombre.lastIndexOf('.') + 1);
            if (seccion.getValue().equals("Clientes")) {
                List<Cliente> visibles = tabla.getItems().stream().map(o -> (Cliente) o).toList();
                formatos.exportar(formato, elegido.toPath(), visibles);
                mensaje("Exportación", "Clientes guardados: " + visibles.size());
            } else if (seccion.getValue().equals("Productos")) {
                List<Producto> visibles = tabla.getItems().stream().map(o -> (Producto) o).toList();
                formatosProducto.escribir(elegido.toPath(), formato, visibles);
                mensaje("Exportación", "Productos guardados: " + visibles.size());
            } else {
                List<Operacion> visibles = tabla.getItems().stream().map(o -> (Operacion) o).toList();
                var resumen = service.resumen(visibles);
                new InformeOperacionesDao().escribir(elegido.toPath(), formato, resumen);
                mensaje("Exportación", "Grupos de cliente y producto guardados: " + resumen.size());
            }
        } catch (Exception ex) { error("No se pudo exportar", ex); }
    }
    private void mensaje(String titulo, String texto) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, texto, ButtonType.OK); a.initOwner(stage); a.setTitle(titulo); a.setHeaderText(null); a.showAndWait();
    }
    private void error(String titulo, Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK); a.initOwner(stage); a.setTitle(titulo); a.setHeaderText(null); a.showAndWait();
    }
    public static void main(String[] args) { launch(args); }
}
