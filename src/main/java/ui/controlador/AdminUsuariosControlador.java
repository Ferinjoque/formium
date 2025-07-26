package ui.controlador;

import aplicacion.GestorDeEstado;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import modelo.usuario.Rol;
import modelo.usuario.Usuario;
import servicio.ServicioNotificacionesUI;
import servicio.ServicioUsuario;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Pestaña “Usuarios” del panel de administración.
 *
 * <ul>
 *   <li>Permite crear, editar y eliminar usuarios.</li>
 *   <li>El control de permisos depende del rol del administrador actual.</li>
 *   <li>Incluye validación básica de formularios y feedback vía
 *       {@link ServicioNotificacionesUI}.</li>
 * </ul>
 */
public class AdminUsuariosControlador implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AdminUsuariosControlador.class.getName());

    /* ----------------------------- FXML -------------------------------- */
    @FXML private ListView<Usuario> listaUsuarios;
    @FXML private TextField        txtNombreUsuario;
    @FXML private PasswordField    txtPasswordUsuario;
    @FXML private PasswordField    txtConfirmarPassword;
    @FXML private ComboBox<Rol>    comboRolUsuario;
    @FXML private GridPane         formGridUsuario;
    @FXML private HBox             botonesUsuario;
    @FXML private Button           btnGuardarUsuario;
    @FXML private Button           btnEliminarUsuario;

    /* ---------------------------- lógica ------------------------------- */
    private final ServicioUsuario   servicioUsuario   = new ServicioUsuario();
    private final ObservableList<Usuario> usuariosObs = FXCollections.observableArrayList();

    private Usuario        adminActual;
    private boolean        enModoCreacion = false;
    private ResourceBundle bundle;

    /* ------------------------------------------------------------------- */
    /*  Ciclo de vida                                                      */
    /* ------------------------------------------------------------------- */

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bundle       = resourceBundle;
        adminActual  = GestorDeEstado.obtenerInstancia().getUsuarioActual();

        configurarPestanaUsuarios();
    }

    /* ------------------------------------------------------------------- */
    /*  Configuración inicial                                              */
    /* ------------------------------------------------------------------- */

    private void configurarPestanaUsuarios() {
        comboRolUsuario.setItems(FXCollections.observableArrayList(Rol.USER, Rol.ADMIN));

        refrescarListaUsuarios();
        listaUsuarios.setItems(usuariosObs);

        listaUsuarios.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Usuario user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null
                        ? null
                        : bundle.getString("admin.usuarios.list.item")
                        .formatted(user.getNombreUsuario(), user.getRol()));
            }
        });

        listaUsuarios.getSelectionModel().selectedItemProperty()
                .addListener((o, ov, sel) -> {
                    enModoCreacion = (sel == null);
                    if (sel != null) cargarDatosUsuarioEnFormulario(sel);
                });

        txtNombreUsuario.textProperty().addListener((o, ov, nv) -> actualizarEstadoBotonGuardar());
        txtPasswordUsuario.textProperty().addListener((o, ov, nv) -> actualizarEstadoBotonGuardar());
        txtConfirmarPassword.textProperty().addListener((o, ov, nv) -> actualizarEstadoBotonGuardar());
        comboRolUsuario.valueProperty().addListener((o, ov, nv) -> actualizarEstadoBotonGuardar());

        deshabilitarFormularioUsuario(true);
    }

    /* ------------------------------------------------------------------- */
    /*  Guardar / actualizar                                               */
    /* ------------------------------------------------------------------- */

    @FXML
    void handleGuardarUsuario(ActionEvent evt) {
        if (enModoCreacion) {
            crearNuevoUsuario(evt);
        } else {
            actualizarUsuarioExistente(evt);
        }
    }

    private void crearNuevoUsuario(ActionEvent evt) {
        String nombre = txtNombreUsuario.getText();
        String pass   = txtPasswordUsuario.getText();

        Usuario nuevo = servicioUsuario.registrarUsuario(nombre, pass);
        if (nuevo == null) {
            notificar(bundle.getString("admin.usuarios.notif.error_usuario_existe.title"),
                    bundle.getString("admin.usuarios.notif.error_usuario_existe.msg").formatted(nombre),
                    evt);
            return;
        }

        if (adminActual.esSuperAdmin()) {
            nuevo.setRol(comboRolUsuario.getValue());
            servicioUsuario.actualizarUsuario(nuevo);
        }

        refrescarListaUsuarios();
        listaUsuarios.getSelectionModel().select(nuevo);

        notificar(bundle.getString("admin.usuarios.notif.exito_crear.title"),
                bundle.getString("admin.usuarios.notif.exito_crear.msg").formatted(nombre),
                evt);
    }

    private void actualizarUsuarioExistente(ActionEvent evt) {
        Usuario sel = listaUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        boolean modificado = false;

        if (sel.getRol() != comboRolUsuario.getValue()) {
            sel.setRol(comboRolUsuario.getValue());
            modificado = true;
        }

        if (!txtPasswordUsuario.getText().isEmpty()) {
            sel.setPasswordHash(servicioUsuario.hashearPassword(txtPasswordUsuario.getText()));
            modificado = true;
        }

        if (modificado) {
            servicioUsuario.actualizarUsuario(sel);
            notificar(bundle.getString("admin.usuarios.notif.exito_actualizar.title"),
                    bundle.getString("admin.usuarios.notif.exito_actualizar.msg").formatted(sel.getNombreUsuario()),
                    evt);
            refrescarListaUsuarios();
            listaUsuarios.getSelectionModel().select(sel);
        }
        limpiarCamposPassword();
    }

    /* ------------------------------------------------------------------- */
    /*  Nuevo                                                              */
    /* ------------------------------------------------------------------- */

    @FXML
    void handleNuevoUsuario(ActionEvent evt) {
        enModoCreacion = true;
        listaUsuarios.getSelectionModel().clearSelection();
        limpiarFormularioUsuario();

        deshabilitarFormularioUsuario(false);
        comboRolUsuario.setValue(Rol.USER);
        comboRolUsuario.setDisable(!adminActual.esSuperAdmin());

        txtNombreUsuario.setDisable(false);
        btnEliminarUsuario.setDisable(true);
        actualizarEstadoBotonGuardar();
        txtNombreUsuario.requestFocus();
    }

    /* ------------------------------------------------------------------- */
    /*  Eliminar                                                           */
    /* ------------------------------------------------------------------- */

    @FXML
    void handleEliminarUsuario(ActionEvent evt) {
        Usuario sel = listaUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        if (sel.getId().equals(adminActual.getId())) {
            notificarAccionNoPermitida("admin.usuarios.notif.no_puedes_eliminarte.msg", evt);
            return;
        }
        if (!adminActual.esSuperAdmin() && sel.esAdmin()) {
            notificarAccionNoPermitida("admin.usuarios.notif.admin_no_elimina_admin.msg", evt);
            return;
        }

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                bundle.getString("admin.usuarios.alert.confirmar_eliminacion.msg").formatted(sel.getNombreUsuario()),
                ButtonType.YES, ButtonType.NO);

        conf.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(res -> {
            servicioUsuario.eliminarUsuario(sel);
            refrescarListaUsuarios();
            handleNuevoUsuario(null);

            notificar(bundle.getString("admin.usuarios.notif.usuario_eliminado.title"),
                    bundle.getString("admin.usuarios.notif.usuario_eliminado.msg").formatted(sel.getNombreUsuario()),
                    evt);
        });
    }

    /* ------------------------------------------------------------------- */
    /*  Auxiliares                                                          */
    /* ------------------------------------------------------------------- */

    private void refrescarListaUsuarios() {
        List<Usuario> todos = servicioUsuario.obtenerTodosLosUsuarios();
        usuariosObs.setAll(
                adminActual.esSuperAdmin()
                        ? todos
                        : todos.stream().filter(u -> !u.esAdmin()).collect(Collectors.toList()));
    }

    private void cargarDatosUsuarioEnFormulario(Usuario u) {
        limpiarFormularioUsuario();
        txtNombreUsuario.setText(u.getNombreUsuario());
        comboRolUsuario.setValue(u.getRol());

        deshabilitarFormularioUsuario(false);
        txtNombreUsuario.setDisable(true);

        boolean superAdmin   = adminActual.esSuperAdmin();
        boolean mismoUsuario = adminActual.getId().equals(u.getId());
        boolean targetAdmin  = u.esAdmin();

        comboRolUsuario.setDisable(!superAdmin || mismoUsuario);
        boolean puedeCambiarPass = mismoUsuario || (superAdmin && !u.esSuperAdmin()) || (!superAdmin && !targetAdmin);
        txtPasswordUsuario.setDisable(!puedeCambiarPass);
        txtConfirmarPassword.setDisable(!puedeCambiarPass);

        btnEliminarUsuario.setDisable(mismoUsuario || (!superAdmin && targetAdmin));
        actualizarEstadoBotonGuardar();
    }

    private void deshabilitarFormularioUsuario(boolean disable) {
        formGridUsuario.setDisable(disable);
        botonesUsuario.setDisable(disable);
        if (disable) limpiarFormularioUsuario();
    }

    private void limpiarFormularioUsuario() {
        txtNombreUsuario.clear();
        limpiarCamposPassword();
        comboRolUsuario.setValue(null);
    }

    private void limpiarCamposPassword() {
        txtPasswordUsuario.clear();
        txtConfirmarPassword.clear();
    }

    private void actualizarEstadoBotonGuardar() {
        boolean disable;
        if (enModoCreacion) {
            boolean nombreOk = !txtNombreUsuario.getText().isBlank();
            boolean passOk   = !txtPasswordUsuario.getText().isEmpty()
                    && txtPasswordUsuario.getText().equals(txtConfirmarPassword.getText());
            disable = !(nombreOk && passOk);
        } else {
            Usuario sel = listaUsuarios.getSelectionModel().getSelectedItem();
            if (sel == null) { btnGuardarUsuario.setDisable(true); return; }

            boolean rolDiff = comboRolUsuario.getValue() != sel.getRol();
            boolean passOk  = !txtPasswordUsuario.getText().isEmpty()
                    && txtPasswordUsuario.getText().equals(txtConfirmarPassword.getText());
            disable = !(rolDiff || passOk);
        }
        btnGuardarUsuario.setDisable(disable);
    }

    private void notificar(String tituloKey, String msgKey, ActionEvent e) {
        ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                tituloKey, msgKey, (Node) e.getSource());
    }

    private void notificarAccionNoPermitida(String msgKey, ActionEvent e) {
        notificar(bundle.getString("admin.usuarios.notif.accion_no_permitida.title"),
                bundle.getString(msgKey), e);
    }
}
