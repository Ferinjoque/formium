package aplicacion;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import observador.GestorInventario;
import observador.ObservadorGerente;
import observador.ObservadorLogistico;
import util.GestorJPA;
import util.InicializadorBD;
import util.Rutas;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Punto de entrada de la aplicación JavaFX “Tienda FORMIUM”.
 *
 * <p>Responsabilidades principales:</p>
 * <ul>
 *   <li>Inicializar la base de datos y registrar observadores del {@link observador.GestorInventario}.</li>
 *   <li>Cargar la vista inicial (pantalla de login) a partir de FXML.</li>
 *   <li>Establecer un manejador global de excepciones que registre el error y
 *       muestre al usuario una alerta localizada.</li>
 *   <li>Cerrar los recursos de JPA al finalizar la aplicación.</li>
 * </ul>
 */
public final class TiendaApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(TiendaApp.class.getName());

    private ResourceBundle bundle;

    /* ---------------------------------------------------------------------- */
    /*  Ciclo de vida JavaFX                                                  */
    /* ---------------------------------------------------------------------- */

    @Override
    public void start(Stage stage) {
        bundle = ResourceBundle.getBundle("ui.vista.messages");

        inicializarBaseDeDatos();
        configurarObservadoresInventario();
        configurarManejadorGlobalExcepciones();

        cargarVistaLogin(stage);
    }

    /**
     * Libera los recursos asociados a JPA antes de que la JVM finalice.
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        GestorJPA.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /* ---------------------------------------------------------------------- */
    /*  Inicialización de infraestructura                                     */
    /* ---------------------------------------------------------------------- */

    private void inicializarBaseDeDatos() {
        InicializadorBD.verificarYPoblarDatosIniciales();
    }

    private void configurarObservadoresInventario() {
        GestorInventario gestorInventario = GestorInventario.obtenerInstancia();
        gestorInventario.anadirObservador(new ObservadorLogistico("CENTRO_DE_DISTRIBUCION_1"));
        gestorInventario.anadirObservador(new ObservadorGerente("Sr. Gerente"));
    }

    private void configurarManejadorGlobalExcepciones() {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.log(Level.SEVERE,
                    "Excepción no capturada en el hilo {0}", thread.getName());
            LOGGER.log(Level.SEVERE, "Traza de la excepción", throwable);

            Platform.runLater(() -> mostrarAlertaErrorFatal(throwable));
        });
    }

    /* ---------------------------------------------------------------------- */
    /*  Carga de vistas                                                       */
    /* ---------------------------------------------------------------------- */

    private void cargarVistaLogin(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(Rutas.VISTAS.LOGIN), bundle);
            Parent root = loader.load();

            stage.setTitle(bundle.getString("window.title.login"));
            stage.setScene(new Scene(root, 600, 500));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al cargar la vista de login", ex);
            mostrarAlertaCargaVista(ex);
        }
    }

    /* ---------------------------------------------------------------------- */
    /*  Alertas de error                                                      */
    /* ---------------------------------------------------------------------- */

    /**
     * Muestra una alerta con la traza de una excepción considerada fatal para
     * la aplicación.
     *
     * @param throwable la excepción causante del fallo
     */
    private void mostrarAlertaErrorFatal(Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("alert.title.error_inesperado"));
        alert.setHeaderText(bundle.getString("app.error.inesperado.header"));
        alert.setContentText(bundle.getString("app.error.inesperado.content"));

        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        String trace = sw.toString();

        Label label = new Label(bundle.getString("app.error.stacktrace_label"));
        TextArea textArea = new TextArea(trace);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        VBox content = new VBox(label, textArea);
        content.setMaxWidth(Double.MAX_VALUE);
        alert.getDialogPane().setExpandableContent(content);

        alert.showAndWait();
    }

    /**
     * Muestra una alerta cuando falla la carga de una vista FXML.
     *
     * @param ex la excepción de E/S lanzada durante la carga
     */
    private void mostrarAlertaCargaVista(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("alert.title.error_carga"));
        alert.setHeaderText(bundle.getString("notif.error.carga_vista").formatted("login"));
        alert.setContentText(ex.getMessage());
        alert.showAndWait();
    }
}
