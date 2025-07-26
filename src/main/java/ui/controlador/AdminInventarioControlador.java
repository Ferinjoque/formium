package ui.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.admin.StockProducto;
import modelo.producto.Producto;
import observador.GestorInventario;
import servicio.ServicioInventario;
import servicio.ServicioNotificacionesUI;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la pestaña “Inventario” del panel de administración.
 */
public class AdminInventarioControlador implements Initializable {

    /* ---------------------- componentes FXML ---------------------- */
    @FXML private TableView<StockProducto>     tablaInventario;
    @FXML private TableColumn<StockProducto, Long>    colId;
    @FXML private TableColumn<StockProducto, String>  colProducto;
    @FXML private TableColumn<StockProducto, String>  colTipo;
    @FXML private TableColumn<StockProducto, Double>  colPrecio;
    @FXML private TableColumn<StockProducto, Integer> colStock;
    @FXML private Label      lblProductoSeleccionado;
    @FXML private TextField  txtCantidadReponer;
    @FXML private Button     btnReponerStock;

    /* --------------------------- lógica --------------------------- */
    private ObservableList<StockProducto> stockObservables;
    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bundle = resourceBundle;
        configurarPestanaInventario();
    }

    /* ------------------------ configuración ----------------------- */
    private void configurarPestanaInventario() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        colId     .prefWidthProperty().bind(tablaInventario.widthProperty().multiply(0.10).subtract(2));
        colProducto.prefWidthProperty().bind(tablaInventario.widthProperty().multiply(0.40).subtract(2));
        colTipo   .prefWidthProperty().bind(tablaInventario.widthProperty().multiply(0.15).subtract(2));
        colPrecio .prefWidthProperty().bind(tablaInventario.widthProperty().multiply(0.15).subtract(2));
        colStock  .prefWidthProperty().bind(tablaInventario.widthProperty().multiply(0.20).subtract(2));

        stockObservables = FXCollections.observableArrayList();
        refrescarTablaInventario();
        tablaInventario.setItems(stockObservables);

        tablaInventario.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean sel = n != null;
            btnReponerStock.setDisable(!sel);
            lblProductoSeleccionado.setText(
                    sel
                            ? String.format(bundle.getString("admin.inventario.label.producto_seleccionado"), n.getNombre())
                            : bundle.getString("admin.inventario.label.seleccionar_producto"));
        });

        btnReponerStock.setDisable(true);
        lblProductoSeleccionado.setText(bundle.getString("admin.inventario.label.seleccionar_producto"));
    }

    /* ------------------------ reponer stock ----------------------- */
    @FXML
    void handleReponerStock(ActionEvent e) {
        StockProducto item = tablaInventario.getSelectionModel().getSelectedItem();
        if (item == null) {
            ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                    bundle.getString("admin.inventario.notif.error.title"),
                    bundle.getString("admin.inventario.notif.error_no_seleccionado.msg"),
                    (Node) e.getSource());
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidadReponer.getText());
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                    bundle.getString("admin.inventario.notif.error_validacion.title"),
                    bundle.getString("admin.inventario.notif.error_validacion.msg"),
                    (Node) e.getSource());
            return;
        }

        Producto prod = item.getProductoOriginal();
        GestorInventario.obtenerInstancia().reponerStock(prod, cantidad);

        txtCantidadReponer.clear();
        refrescarTablaInventario();
        tablaInventario.getSelectionModel().clearSelection();
    }

    /* ----------------------- refrescar tabla ---------------------- */
    public void refrescarTablaInventario() {
        stockObservables.clear();
        new ServicioInventario().obtenerTodoElInventario()
                .forEach(inv -> stockObservables.add(
                        new StockProducto(inv.getProducto(), inv.getCantidad())));
    }
}
