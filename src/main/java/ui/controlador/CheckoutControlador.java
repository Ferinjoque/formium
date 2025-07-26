package ui.controlador;

import aplicacion.GestorDeEstado;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import modelo.carrito.CarritoCompras;
import modelo.pedido.ItemPedido;
import modelo.pedido.Pedido;
import modelo.producto.Producto;
import modelo.producto.ProductoBase;
import modelo.usuario.Usuario;
import observador.GestorInventario;
import servicio.ServicioNotificacionesUI;
import servicio.ServicioPedido;
import util.Rutas;
import util.json.GsonUtil;

import java.io.IOException;
import java.net.URL;
import java.time.YearMonth;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Flujo de checkout: valida el formulario de pago, crea el {@link Pedido} y
 * actualiza el stock e histórico globales.
 */
public class CheckoutControlador implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(CheckoutControlador.class.getName());

    /* ------------------------------ FXML ------------------------------ */
    @FXML private Label             lblTotalPagar;
    @FXML private ComboBox<String>  comboMetodoPago;
    @FXML private TextField         txtNumeroTarjeta, txtNombre, txtApellido,
            txtCiudad, txtDireccion, txtCodigoPostal,
            txtPais, txtTelefono;
    @FXML private ComboBox<Integer> comboMesExpiracion, comboAnioExpiracion;
    @FXML private PasswordField     txtCVV;
    @FXML private Button            btnPagar;

    /* ----------------------------- lógica ----------------------------- */
    private CarritoCompras carrito;
    private Usuario        usuarioActual;
    private final ServicioPedido servicioPedido = new ServicioPedido();
    private ResourceBundle bundle;

    /* ------------------------------------------------------------------ */
    /*  Inicialización                                                    */
    /* ------------------------------------------------------------------ */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bundle        = resourceBundle;
        carrito       = GestorDeEstado.obtenerInstancia().getCarrito();
        usuarioActual = GestorDeEstado.obtenerInstancia().getUsuarioActual();

        poblarMetodosDePago();
        poblarFechasExpiracion();
        configurarValidaciones();
        validarFormulario();
    }

    public void setDatosCompra(double total) {
        lblTotalPagar.setText("S/%.2f".formatted(total));
    }

    /* ------------------------------------------------------------------ */
    /*  Helpers de formulario                                             */
    /* ------------------------------------------------------------------ */

    private void poblarMetodosDePago() {
        comboMetodoPago.setItems(FXCollections.observableArrayList(
                "Visa", "MasterCard", "PagoEfectivo", "BCP", "BBVA Continental"));
    }

    private void poblarFechasExpiracion() {
        comboMesExpiracion.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList())));
        int yearActual = YearMonth.now().getYear();
        comboAnioExpiracion.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(yearActual, yearActual + 10).boxed().collect(Collectors.toList())));
    }

    private void configurarValidaciones() {
        txtNumeroTarjeta.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d{0,16}") ? c : null));
        txtCVV.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d{0,4}") ? c : null));
        txtTelefono.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d{0,9}") ? c : null));

        Runnable validator = this::validarFormulario;
        txtNombre.textProperty().addListener((o, ov, nv) -> validator.run());
        txtApellido.textProperty().addListener((o, ov, nv) -> validator.run());
        txtDireccion.textProperty().addListener((o, ov, nv) -> validator.run());
        txtCiudad.textProperty().addListener((o, ov, nv) -> validator.run());
        txtCodigoPostal.textProperty().addListener((o, ov, nv) -> validator.run());
        txtTelefono.textProperty().addListener((o, ov, nv) -> validator.run());
        comboMetodoPago.valueProperty().addListener((o, ov, nv) -> validator.run());
        txtNumeroTarjeta.textProperty().addListener((o, ov, nv) -> validator.run());
        comboMesExpiracion.valueProperty().addListener((o, ov, nv) -> validator.run());
        comboAnioExpiracion.valueProperty().addListener((o, ov, nv) -> validator.run());
        txtCVV.textProperty().addListener((o, ov, nv) -> validator.run());
    }

    private void validarFormulario() {
        boolean ok =
                !txtNombre.getText().isBlank() &&
                        !txtApellido.getText().isBlank() &&
                        !txtCiudad.getText().isBlank() &&
                        !txtDireccion.getText().isBlank() &&
                        !txtCodigoPostal.getText().isBlank() &&
                        txtTelefono.getText().length() == 9 &&
                        comboMetodoPago.getValue() != null &&
                        txtNumeroTarjeta.getText().length() == 16 &&
                        comboMesExpiracion.getValue() != null &&
                        comboAnioExpiracion.getValue() != null &&
                        (txtCVV.getText().length() == 3 || txtCVV.getText().length() == 4);

        btnPagar.setDisable(!ok);
    }

    /* ------------------------------------------------------------------ */
    /*  Pago y creación de pedido                                         */
    /* ------------------------------------------------------------------ */

    @FXML
    void handlePagarAhora(ActionEvent e) {
        try {
            String idPedido = "PED_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String direccion = "%s, %s, %s".formatted(
                    txtDireccion.getText(), txtCiudad.getText(), txtCodigoPostal.getText());

            Pedido.ConstructorPedido builder = new Pedido.ConstructorPedido(usuarioActual, idPedido)
                    .establecerDireccionEnvio(direccion)
                    .establecerMetodoPago(comboMetodoPago.getValue());

            Gson gson = GsonUtil.getGson();

            for (Producto p : carrito.obtenerProductos()) {
                ItemPedido it = new ItemPedido();
                it.setProductoBase((ProductoBase) p);
                it.setCantidad(1);
                it.setPrecioUnitario(p.calcularPrecio());
                it.setPersonalizacionJson(gson.toJson(((ProductoBase) p).obtenerElementosDiseno()));
                builder.anadirItem(it);
            }

            Pedido nuevo = builder.construir();
            servicioPedido.guardarNuevoPedido(nuevo);
            GestorDeEstado.obtenerInstancia().getPedidosCreados().add(nuevo);

            nuevo.getItems().forEach(it ->
                    GestorInventario.obtenerInstancia()
                            .venderProducto(it.getProductoBase(), it.getCantidad()));

            carrito.vaciarCarrito();
            navegarPrincipalConExito(e, nuevo);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error en el proceso de pago", ex);
            ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                    bundle.getString("checkout.notif.error.title"),
                    bundle.getString("checkout.notif.error.msg"),
                    (Node) e.getSource());
        }
    }

    private void navegarPrincipalConExito(ActionEvent e, Pedido p) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(Rutas.VISTAS.PRINCIPAL), bundle);
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root, 1_000, 700));
                stage.setResizable(false);
                stage.centerOnScreen();
                stage.show();

                ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                        bundle.getString("checkout.notif.exito.title"),
                        bundle.getString("checkout.notif.exito.msg").formatted(p.obtenerIdPedido()),
                        root);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Error al volver a la pantalla principal", ex);
            }
        });
    }

    /* ------------------------------------------------------------------ */
    /*  Volver sin pagar                                                  */
    /* ------------------------------------------------------------------ */

    @FXML
    void handleVolver(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Rutas.VISTAS.PRINCIPAL), bundle);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1_000, 700));
            stage.setTitle(bundle.getString("window.title.principal"));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error de navegación al volver", ex);
            ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                    bundle.getString("notif.error.title"),
                    bundle.getString("notif.error.carga_vista").formatted("principal"),
                    (Node) e.getSource());
        }
    }
}
