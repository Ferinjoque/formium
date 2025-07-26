package ui.controlador;

import aplicacion.GestorDeEstado;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import modelo.usuario.Usuario;
import servicio.ServicioUsuario;
import util.Rutas;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador de la ventana de inicio de sesión.
 */
public class LoginControlador implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(LoginControlador.class.getName());
    private static final int    MAX_INTENTOS = 3;

    /* ------------------------------- FXML ------------------------------- */
    @FXML private TextField     txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblError;

    /* ------------------------------ lógica ------------------------------ */
    private final ServicioUsuario servicioUsuario = new ServicioUsuario();
    private ResourceBundle bundle;
    private int intentosRestantes = MAX_INTENTOS;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bundle = resourceBundle;
    }

    /* ------------------------------------------------------------------- */
    /*  Login                                                              */
    /* ------------------------------------------------------------------- */
    @FXML
    void handleLogin(ActionEvent event) {
        String nombre   = txtUsuario.getText();
        String password = txtPassword.getText();

        if (nombre.isBlank() || password.isBlank()) {
            lblError.setText(bundle.getString("notif.login.campos_vacios"));
            return;
        }

        Optional<Usuario> auth = servicioUsuario.autenticarUsuario(nombre, password);
        if (auth.isPresent()) {
            GestorDeEstado.obtenerInstancia().setUsuarioActual(auth.get());
            LOGGER.info(() -> "Login exitoso para " + nombre);
            navegarAPrincipal(event);
        } else {
            gestionarIntentoFallido(event);
        }
    }

    /* ------------------------------------------------------------------- */
    /*  Helpers                                                            */
    /* ------------------------------------------------------------------- */

    private void gestionarIntentoFallido(ActionEvent event) {
        intentosRestantes--;
        txtPassword.clear();

        if (intentosRestantes > 0) {
            lblError.setText(bundle.getString("notif.login.error_credenciales").formatted(intentosRestantes));
            return;
        }

        /* bloqueo tras exceder intentos */
        txtUsuario.setDisable(true);
        txtPassword.setDisable(true);
        ((Node) event.getSource()).setDisable(true);

        new Thread(() -> {
            try {
                for (int i = 5; i > 0; i--) {
                    int s = i;
                    Platform.runLater(() ->
                            lblError.setText(bundle.getString("notif.login.bloqueado").formatted(s)));
                    Thread.sleep(1_000);
                }
                Platform.exit();
            } catch (InterruptedException ie) {
                LOGGER.log(Level.SEVERE, "Interrupción en temporizador de bloqueo", ie);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void navegarAPrincipal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Rutas.VISTAS.PRINCIPAL), bundle);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1_000, 700));
            stage.setTitle(bundle.getString("window.title.principal"));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al cargar vista principal", ex);
            lblError.setText(bundle.getString("login.error.navegacion"));
        }
    }
}
