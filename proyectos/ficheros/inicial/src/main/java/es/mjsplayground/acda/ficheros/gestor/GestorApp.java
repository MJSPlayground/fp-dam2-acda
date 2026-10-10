package es.mjsplayground.acda.ficheros.gestor;

import java.io.InputStream;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class GestorApp extends Application {
    private final ComboBox<String> seccion = new ComboBox<>();
    private final TextField busqueda = new TextField();
    private final TableView<String> tabla = new TableView<>();
    private final Button editar = new Button("Editar"), eliminar = new Button("Eliminar");
    private Stage ventana;

    @Override public void start(Stage stage) {
        ventana = stage;
        HBox cabecera = new HBox(10); cabecera.getStyleClass().add("cabecera");
        try (InputStream in = getClass().getResourceAsStream("/branding/mjs-playground.png")) {
            if (in != null) {
                Image logo = new Image(in); stage.getIcons().add(logo);
                ImageView vista = new ImageView(logo); vista.setFitWidth(28); vista.setFitHeight(28);
                cabecera.getChildren().add(vista);
            }
        } catch (Exception ex) { /* La navegación sigue disponible si falla el icono. */ }
        Label marca = new Label("MJS Playground"); marca.getStyleClass().add("marca"); cabecera.getChildren().add(marca);

        seccion.getItems().addAll("Clientes", "Productos", "Operaciones"); seccion.getSelectionModel().selectFirst();
        seccion.setOnAction(e -> configurarTabla());
        busqueda.setPromptText("Buscar en la tabla"); busqueda.textProperty().addListener((o, a, b) -> aplicarFiltro());
        HBox.setHgrow(busqueda, Priority.ALWAYS);
        Button nuevo = new Button("Nuevo"), importar = new Button("Importar"), exportar = new Button("Exportar");
        nuevo.getStyleClass().add("principal"); nuevo.setOnAction(e -> formulario());
        editar.setOnAction(e -> formulario()); eliminar.setOnAction(e -> pendiente("Eliminar"));
        importar.setOnAction(e -> pendiente("Importar")); exportar.setOnAction(e -> pendiente("Exportar"));
        HBox herramientas = new HBox(8, seccion, busqueda, nuevo, editar, eliminar, importar, exportar);
        herramientas.getStyleClass().add("herramientas");
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPlaceholder(new Label("No hay registros para esta búsqueda"));
        tabla.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> actualizarAcciones());
        BorderPane root = new BorderPane(); root.setTop(new VBox(cabecera, herramientas)); root.setCenter(tabla);
        Label estado = new Label("Interfaz preparada · Datos pendientes de implementar"); estado.getStyleClass().add("estado");
        root.setBottom(estado);
        Scene escena = new Scene(root); escena.getStylesheets().add(getClass().getResource("/gestor.css").toExternalForm());
        stage.setScene(escena); stage.setTitle("Gestor comercial · MJS Playground");
        Rectangle2D visual = Screen.getPrimary().getVisualBounds();
        stage.setWidth(Math.min(1080, visual.getWidth() * .88)); stage.setHeight(Math.min(700, visual.getHeight() * .84));
        stage.setMinWidth(700); stage.setMinHeight(460);
        configurarTabla(); stage.show();
    }
    private void configurarTabla() {
        tabla.getColumns().clear(); tabla.setItems(FXCollections.observableArrayList());
        String[] columnas = switch (seccion.getValue()) {
            case "Clientes" -> new String[]{"ID", "Nombre", "Ciudad", "Correo"};
            case "Productos" -> new String[]{"Código", "Nombre", "Unidades", "Precio"};
            default -> new String[]{"ID", "Cliente", "Productos", "Importe"};
        };
        for (String nombre : columnas) tabla.getColumns().add(new TableColumn<>(nombre));
        actualizarAcciones();
    }
    private void aplicarFiltro() { /* La búsqueda se conectará a los registros del proyecto. */ }
    private void actualizarAcciones() {
        boolean seleccionado = tabla.getSelectionModel().getSelectedItem() != null;
        editar.setDisable(!seleccionado || seccion.getValue().equals("Operaciones"));
        eliminar.setDisable(!seleccionado || seccion.getValue().equals("Operaciones"));
    }
    private void formulario() {
        String[] etiquetas = switch (seccion.getValue()) {
            case "Clientes" -> new String[]{"ID", "Nombre", "Ciudad", "Correo"};
            case "Productos" -> new String[]{"Código", "Nombre", "Unidades", "Precio"};
            default -> new String[]{"ID", "Cliente ID", "Productos (código:cantidad)"};
        };
        GridPane grid = new GridPane(); grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(18));
        for (int i = 0; i < etiquetas.length; i++) {
            grid.add(new Label(etiquetas[i]), 0, i);
            grid.add(new TextField(), 1, i);
        }
        Dialog<ButtonType> dialogo = new Dialog<>(); dialogo.initOwner(ventana);
        String singular = seccion.getValue().equals("Clientes") ? "cliente"
                : seccion.getValue().equals("Productos") ? "producto" : "operación";
        dialogo.setTitle("Nuevo " + singular);
        dialogo.getDialogPane().setContent(grid); dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialogo.getDialogPane().getStylesheets().add(getClass().getResource("/gestor.css").toExternalForm());
        if (dialogo.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) pendiente("Guardar");
    }
    private void pendiente(String accion) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, "Completa la lógica de " + accion.toLowerCase() + " según el enunciado.", ButtonType.OK);
        alerta.initOwner(ventana); alerta.setTitle(accion); alerta.setHeaderText(null); alerta.showAndWait();
    }
    public static void main(String[] args) { launch(args); }
}
