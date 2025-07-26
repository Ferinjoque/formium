package ui.controlador;

import aplicacion.GestorDeEstado;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import modelo.carrito.CarritoCompras;
import modelo.catalogo.CatalogoProductos;
import modelo.producto.Producto;
import modelo.producto.ProductoBase;
import modelo.usuario.Usuario;
import servicio.ServicioInventario;
import servicio.ServicioNotificacionesUI;
import util.Rutas;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Vista principal para navegación del cliente, carrito y acceso a panel de
 * administración (según rol).
 */
public class PrincipalControlador implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(PrincipalControlador.class.getName());

    /* ------------------------------ FXML ------------------------------ */
    @FXML private ListView<Producto> listaProductos;
    @FXML private ListView<Producto> listaCarrito;
    @FXML private Label  lblTotalCarrito;
    @FXML private Button btnPersonalizar, btnPanelAdmin, btnVaciarCarrito;

    /* ----------------------------- modelo ----------------------------- */
    private CarritoCompras carrito;
    private Usuario        usuarioActual;
    private ResourceBundle bundle;

    /* ------------------------------------------------------------------ */
    /*  Inicialización                                                    */
    /* ------------------------------------------------------------------ */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bundle = resourceBundle;

        carrito       = GestorDeEstado.obtenerInstancia().getCarrito();
        usuarioActual = GestorDeEstado.obtenerInstancia().getUsuarioActual();
        btnPanelAdmin.setVisible(usuarioActual != null && usuarioActual.esAdmin());

        configurarListadoProductos();
        configurarCeldasCarrito();

        listaCarrito.setItems(carrito.obtenerProductos());
        actualizarTotalCarrito();
        carrito.obtenerProductos().addListener((ListChangeListener<? super Producto>) c -> actualizarTotalCarrito());

        btnVaciarCarrito.visibleProperty().bind(Bindings.isNotEmpty(carrito.obtenerProductos()));
        btnVaciarCarrito.managedProperty().bind(btnVaciarCarrito.visibleProperty());
        btnPersonalizar.disableProperty().bind(listaProductos.getSelectionModel().selectedItemProperty().isNull());
    }

    /* ------------------------------------------------------------------ */
    /*  Configuración de listas                                           */
    /* ------------------------------------------------------------------ */

    private void configurarListadoProductos() {
        CatalogoProductos catalogo = CatalogoProductos.obtenerInstancia();
        List<Producto> productos = catalogo.obtenerTodosLosProductos();

        Map<String, List<Producto>> agrupados = productos.stream()
                .collect(Collectors.groupingBy(Producto::obtenerNombre));
        List<Producto> representantes = agrupados.values().stream()
                .map(l -> l.get(0))
                .toList();

        listaProductos.setItems(FXCollections.observableArrayList(representantes));
        configurarCeldasProducto(agrupados);
    }

    private void configurarCeldasProducto(Map<String, List<Producto>> agrupados) {
        ServicioInventario svc = new ServicioInventario();

        listaProductos.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setDisable(false); getStyleClass().remove("producto-agotado");
                    return;
                }

                List<Producto> variantes = agrupados.get(item.obtenerNombre());
                double precioMin = variantes.stream()
                        .mapToDouble(p -> ((ProductoBase) p).getPrecioBase())
                        .min().orElse(0);

                int stockTotal = variantes.stream()
                        .mapToInt(v -> svc.obtenerStockDeProducto((ProductoBase) v))
                        .sum();

                setText(bundle.getString("principal.list.producto_desde").formatted(
                        item.obtenerNombre(), precioMin));

                boolean agotado = stockTotal <= 0;
                setDisable(agotado);
                if (agotado) getStyleClass().add("producto-agotado");
                else         getStyleClass().remove("producto-agotado");
            }
        });
    }

    private void configurarCeldasCarrito() {
        listaCarrito.setCellFactory(lv -> new ListCell<>() {
            private final Label  lbl   = new Label();
            private final Button btn   = new Button(bundle.getString("principal.btn.eliminar_carrito"));
            private final StackPane pane = new StackPane(lbl, btn);

            {
                lbl.setWrapText(true);
                StackPane.setAlignment(lbl, Pos.CENTER_LEFT);
                StackPane.setMargin(lbl, new Insets(0, 30, 0, 0));
                btn.getStyleClass().add("icon-delete-button");
                StackPane.setAlignment(btn, Pos.CENTER_RIGHT);
            }

            @Override protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }

                lbl.setText(bundle.getString("principal.list.carrito_item")
                        .formatted(item.obtenerNombre(), item.calcularPrecio()));
                btn.setOnAction(ev -> carrito.removerProducto(item));
                setGraphic(pane);
            }
        });
    }

    /* ------------------------------------------------------------------ */
    /*  Acciones                                                          */
    /* ------------------------------------------------------------------ */

    @FXML
    private void procederAlPago(ActionEvent e) {
        if (carrito.obtenerProductos().isEmpty()) {
            notificar("Carrito Vacío", "Añade productos antes de proceder al pago.", (Node) e.getSource());
            return;
        }
        cargarVista(Rutas.VISTAS.CHECKOUT, 600, 700, "window.title.checkout", e,
                ctrl -> ((CheckoutControlador) ctrl).setDatosCompra(carrito.calcularTotal()));
    }

    @FXML
    private void mostrarMisPedidos(ActionEvent e) {
        cargarVista(Rutas.VISTAS.PEDIDOS, 1_000, 700, "window.title.mis_pedidos", e,
                ctrl -> ((PedidosControlador) ctrl)
                        .setPedidos(GestorDeEstado.obtenerInstancia().getPedidosCreados(), usuarioActual));
    }

    @FXML
    private void personalizarProducto(ActionEvent e) {
        Producto seleccionado = listaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Rutas.VISTAS.PERSONALIZACION), bundle);
            Parent root = loader.load();
            PersonalizacionControlador pc = loader.getController();
            pc.setProducto(seleccionado);

            Stage st = new Stage();
            st.setTitle(bundle.getString("window.title.personalizacion").formatted(seleccionado.obtenerNombre()));
            st.setScene(new Scene(root, 1_000, 750));
            st.setResizable(false);
            st.setOnShown(ev -> { pc.refrescarLayoutInicial(); st.centerOnScreen(); });
            st.show();

            ((Stage) ((Node) e.getSource()).getScene().getWindow()).close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error cargando personalización", ex);
            notificarCarga("personalización", e);
        }
    }

    @FXML
    private void vaciarCarrito(ActionEvent e) {
        if (carrito.obtenerProductos().isEmpty()) return;

        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                bundle.getString("alert.content.vaciar_carrito"),
                ButtonType.OK, ButtonType.CANCEL);
        a.setTitle(bundle.getString("alert.title.confirmacion"));
        a.setHeaderText(bundle.getString("alert.header.vaciar_carrito"));
        if (a.showAndWait().filter(b -> b == ButtonType.OK).isPresent()) carrito.vaciarCarrito();
    }

    @FXML
    private void mostrarPanelAdmin(ActionEvent e) {
        cargarVista(Rutas.VISTAS.ADMIN, 1_000, 700, "window.title.admin", e, null);
    }

    @FXML
    private void handleCerrarSesion(ActionEvent e) {
        GestorDeEstado.obtenerInstancia().setUsuarioActual(null);
        cargarVista(Rutas.VISTAS.LOGIN, 600, 500, "window.title.login", e, null);
    }

    /* ------------------------------------------------------------------ */
    /*  Helpers                                                           */
    /* ------------------------------------------------------------------ */

    private void cargarVista(String fxml, int w, int h, String titleKey,
                             ActionEvent e, java.util.function.Consumer<Object> onLoaded) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml), bundle);
            Parent root = loader.load();
            if (onLoaded != null) onLoaded.accept(loader.getController());

            Stage st = (Stage) ((Node) e.getSource()).getScene().getWindow();
            st.setScene(new Scene(root, w, h));
            st.setTitle(bundle.getString(titleKey));
            st.setResizable(false);
            st.centerOnScreen();
            st.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error cargando vista: " + fxml, ex);
            notificarCarga(fxml, e);
        }
    }

    private void notificarCarga(String nombreVista, ActionEvent e) {
        notificar(bundle.getString("alert.title.error_carga"),
                bundle.getString("notif.error.carga_vista").formatted(nombreVista), (Node) e.getSource());
    }

    private void notificar(String titulo, String msg, Node owner) {
        ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(titulo, msg, owner);
    }

    private void actualizarTotalCarrito() {
        lblTotalCarrito.setText(bundle.getString("principal.label.total_carrito")
                .formatted(carrito.calcularTotal()));
    }
}
