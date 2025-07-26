package ui.controlador;

import aplicacion.GestorDeEstado;
import comando.*;
import estado.ContextoEstadoPedido;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import modelo.pedido.Pedido;
import modelo.usuario.Usuario;
import observador.GestorInventario;
import servicio.ServicioNotificacionesUI;
import servicio.ServicioPedido;
import servicio.ServicioUsuario;
import util.Rutas;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador de la pestaña “Pedidos” del panel de administración.
 * <p>
 * Permite cambiar el estado de los pedidos con el patrón Command, abrir
 * detalles y, en caso de cancelación, reponer el stock correspondiente.
 * </p>
 */
public class AdminPedidosControlador implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AdminPedidosControlador.class.getName());

    /* ------------------ inyección del controlador de inventario ------------------ */
    private AdminInventarioControlador adminInventarioControlador;

    /* --------------------------------- FXML -------------------------------------- */
    @FXML private ListView<Pedido> listaTodosPedidos;
    @FXML private Button btnProcesarPedido;
    @FXML private Button btnEnviarPedido;
    @FXML private Button btnEntregarPedido;
    @FXML private Button btnCancelarPedido;

    /* --------------------------------- lógica ------------------------------------ */
    private final Map<String, ContextoEstadoPedido> contextosPedidos = new HashMap<>();
    private final InvocadorComando invocadorComando = new InvocadorComando();
    private final ServicioUsuario  servicioUsuario  = new ServicioUsuario();
    private final ServicioPedido   servicioPedido   = new ServicioPedido();

    private ResourceBundle bundle;

    /* ----------------------------------------------------------------------------- */
    /*  Ciclo de vida                                                                */
    /* ----------------------------------------------------------------------------- */

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bundle = resourceBundle;
        configurarPestanaPedidos();
    }

    public void setAdminInventarioControlador(AdminInventarioControlador ctrl) {
        adminInventarioControlador = ctrl;
    }

    /* ----------------------------------------------------------------------------- */
    /*  Configuración                                                                */
    /* ----------------------------------------------------------------------------- */

    private void configurarPestanaPedidos() {

        listaTodosPedidos.setItems(GestorDeEstado.obtenerInstancia().getPedidosCreados());

        GestorDeEstado.obtenerInstancia().getPedidosCreados()
                .forEach(p -> contextosPedidos.put(p.obtenerIdPedido(), new ContextoEstadoPedido(p)));

        listaTodosPedidos.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Pedido item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String nombreUsuario = Optional.ofNullable(item.getUsuario())
                            .map(Usuario::getNombreUsuario)
                            .orElse(bundle.getString("admin.pedidos.usuario_no_disponible"));
                    setText(String.format(
                            bundle.getString("admin.pedidos.list.item"),
                            item.obtenerIdPedido(),
                            nombreUsuario,
                            item.obtenerEstado(),
                            item.obtenerCostoTotal()));
                }
            }
        });

        listaTodosPedidos.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2) {
                abrirVentanaDeDetalles(listaTodosPedidos.getSelectionModel().getSelectedItem());
            }
        });

        listaTodosPedidos.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> actualizarEstadoBotones(n));

        actualizarEstadoBotones(null);
    }

    /* ----------------------------------------------------------------------------- */
    /*  Habilitar / deshabilitar botones según el estado                             */
    /* ----------------------------------------------------------------------------- */

    private void actualizarEstadoBotones(Pedido p) {
        if (p == null) {
            btnProcesarPedido.setDisable(true);
            btnEnviarPedido.setDisable(true);
            btnEntregarPedido.setDisable(true);
            btnCancelarPedido.setDisable(true);
            return;
        }
        String estado = p.obtenerEstado();
        boolean pendiente  = "PENDIENTE".equals(estado);
        boolean procesando = "PROCESANDO".equals(estado);
        boolean enviado    = "ENVIADO".equals(estado);
        boolean finalizado = "ENTREGADO".equals(estado) || "CANCELADO".equals(estado);

        btnProcesarPedido.setDisable(!pendiente);
        btnEnviarPedido  .setDisable(!procesando);
        btnEntregarPedido.setDisable(!enviado);
        btnCancelarPedido.setDisable(finalizado || enviado);
    }

    /* ----------------------------------------------------------------------------- */
    /*  Handlers de botones                                                          */
    /* ----------------------------------------------------------------------------- */

    @FXML void handleProcesarPedido(ActionEvent e) {
        ejecutarComandoDeEstado(e, ComandoProcesarPedido::new,
                bundle.getString("admin.pedidos.accion.procesado"));
    }

    @FXML void handleEnviarPedido(ActionEvent e) {
        ejecutarComandoDeEstado(e, ComandoEnviarPedido::new,
                bundle.getString("admin.pedidos.accion.enviado"));
    }

    @FXML void handleEntregarPedido(ActionEvent e) {
        ejecutarComandoDeEstado(e, ComandoEntregarPedido::new,
                bundle.getString("admin.pedidos.accion.entregado"));
    }

    @FXML void handleCancelarPedido(ActionEvent e) {
        ejecutarComandoDeEstado(e, ComandoCancelarPedido::new,
                bundle.getString("admin.pedidos.accion.cancelado"));
    }

    /* ----------------------------------------------------------------------------- */
    /*  Cambio de estado mediante patrón Command                                     */
    /* ----------------------------------------------------------------------------- */

    private void ejecutarComandoDeEstado(ActionEvent ev,
                                         ComandoFactory factory,
                                         String accionLocalizada) {

        Node ownerNode = (Node) ev.getSource();
        Pedido pedidoSel = listaTodosPedidos.getSelectionModel().getSelectedItem();
        if (pedidoSel == null) return;

        boolean esCancelacion = accionLocalizada.equals(bundle.getString("admin.pedidos.accion.cancelado"));

        /* ---------------- validación de cancelación ---------------- */
        if (esCancelacion && !validarPasswordAdministrador(ownerNode)) {
            return;
        }

        String estadoAntes = pedidoSel.obtenerEstado();
        ContextoEstadoPedido ctx = contextosPedidos
                .computeIfAbsent(pedidoSel.obtenerIdPedido(), id -> new ContextoEstadoPedido(pedidoSel));

        invocadorComando.ejecutarComando(factory.create(ctx, pedidoSel));
        String estadoDespues = pedidoSel.obtenerEstado();

        if (!estadoAntes.equals(estadoDespues)) {
            servicioPedido.actualizarPedido(pedidoSel);
            ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                    bundle.getString("admin.pedidos.notif.exito.title"),
                    String.format(bundle.getString("admin.pedidos.notif.exito.msg"), accionLocalizada),
                    ownerNode);

            if (esCancelacion) {
                reponerStockPorCancelacion(pedidoSel);
                Optional.ofNullable(adminInventarioControlador).ifPresent(AdminInventarioControlador::refrescarTablaInventario);
            }
        } else {
            ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                    bundle.getString("admin.pedidos.notif.accion_invalida.title"),
                    String.format(bundle.getString("admin.pedidos.notif.accion_invalida.msg"),
                            accionLocalizada, estadoAntes),
                    ownerNode);
        }

        listaTodosPedidos.refresh();
        int idx = listaTodosPedidos.getSelectionModel().getSelectedIndex();
        listaTodosPedidos.getSelectionModel().clearSelection();
        Platform.runLater(() -> listaTodosPedidos.getSelectionModel().select(idx));
    }

    /* ----------------------------------------------------------------------------- */
    /*  Reposición automática de stock al cancelar                                   */
    /* ----------------------------------------------------------------------------- */

    private void reponerStockPorCancelacion(Pedido p) {
        GestorInventario gestor = GestorInventario.obtenerInstancia();
        p.getItems().forEach(i -> gestor.reponerStock(i.getProductoBase(), i.getCantidad()));
    }

    /* ----------------------------------------------------------------------------- */
    /*  Diálogo de confirmación de contraseña para cancelación                       */
    /* ----------------------------------------------------------------------------- */

    private boolean validarPasswordAdministrador(Node ownerNode) {
        Optional<String> pwdRes = mostrarDialogoConfirmacionPassword();
        if (pwdRes.isEmpty() || pwdRes.get().isBlank()) return false;

        Usuario admin = GestorDeEstado.obtenerInstancia().getUsuarioActual();
        boolean ok = servicioUsuario.autenticarUsuario(admin.getNombreUsuario(), pwdRes.get()).isPresent();

        if (!ok) {
            ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                    bundle.getString("admin.pedidos.notif.error_auth.title"),
                    bundle.getString("admin.pedidos.notif.error_auth.msg"),
                    ownerNode);
        }
        return ok;
    }

    private Optional<String> mostrarDialogoConfirmacionPassword() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(bundle.getString("admin.pedidos.dialog.title"));
        dialog.setHeaderText(bundle.getString("admin.pedidos.dialog.header"));

        ButtonType btnConfirmar = new ButtonType(
                bundle.getString("admin.pedidos.dialog.btn_confirmar"),
                ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirmar, ButtonType.CANCEL);

        PasswordField pwd = new PasswordField();
        pwd.setPromptText(bundle.getString("admin.pedidos.dialog.label"));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        grid.add(new Label(bundle.getString("admin.pedidos.dialog.label")), 0, 0);
        grid.add(pwd, 1, 0);
        dialog.getDialogPane().setContent(grid);

        Platform.runLater(pwd::requestFocus);
        dialog.setResultConverter(btn -> btn == btnConfirmar ? pwd.getText() : null);

        return dialog.showAndWait();
    }

    /* ----------------------------------------------------------------------------- */
    /*  Ventana de detalles                                                          */
    /* ----------------------------------------------------------------------------- */

    private void abrirVentanaDeDetalles(Pedido pedido) {
        if (pedido == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(Rutas.VISTAS.DETALLES_PEDIDO), bundle);
            Parent root = loader.load();
            DetallesPedidoControlador ctrl = loader.getController();
            ctrl.setPedido(pedido);

            Stage st = new Stage();
            st.setTitle(bundle.getString("admin.pedidos.detalles.title"));
            st.setScene(new Scene(root));
            st.setResizable(false);
            st.centerOnScreen();
            st.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al abrir la ventana de detalles de pedido", ex);
            ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                    bundle.getString("admin.pedidos.detalles.error.title"),
                    bundle.getString("admin.pedidos.detalles.error.msg"),
                    null);
        }
    }

    /* ----------------------------------------------------------------------------- */
    /*  Factory funcional para construir comandos                                    */
    /* ----------------------------------------------------------------------------- */
    @FunctionalInterface
    interface ComandoFactory {
        Comando create(ContextoEstadoPedido ctx, Pedido p);
    }
}
