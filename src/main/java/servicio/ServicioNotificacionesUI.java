package servicio;

import aplicacion.GestorDeEstado;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import modelo.usuario.Usuario;
import org.controlsfx.control.Notifications;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fachada que centraliza la creación de notificaciones JavaFX
 * (incluye anti-spam y «debouncing» para alertas de stock).
 */
public class ServicioNotificacionesUI {

    /* --------------------------- Singleton --------------------------- */
    private static ServicioNotificacionesUI instancia;
    public static synchronized ServicioNotificacionesUI obtenerInstancia() {
        if (instancia == null) instancia = new ServicioNotificacionesUI();
        return instancia;
    }

    /* ----------------------------- log -------------------------------- */
    private static final Logger LOGGER = Logger.getLogger(ServicioNotificacionesUI.class.getName());

    /* -------------- cooldown global (anti-spam) ----------------------- */
    private static final int  MAX_NOTIFS_WINDOW = 5;
    private static final long TIME_WINDOW_MS    = 5_000;
    private final Deque<Long> timestamps        = new ArrayDeque<>();

    /* ------------- debounce de alertas de stock ---------------------- */
    private final Map<String, TimerTask> stockTimers = new HashMap<>();
    private final Timer debouncer = new Timer("NotificationDebouncer", true);

    private ServicioNotificacionesUI() { }

    /* ----------------------- API pública ------------------------------ */

    public void mostrarNotificacion(String titulo, String texto) {
        mostrarNotificacion(titulo, texto, null);
    }

    public void mostrarNotificacion(String titulo, String texto, Node owner) {
        if (!permitirNuevaNotificacion()) {
            LOGGER.fine(() -> "Notificación bloqueada por spam: " + titulo);
            return;
        }
        _displayNotification(titulo, texto, owner);
    }

    public void notificarAlertaDeStock(String productoId, String titulo,
                                       String texto, Node owner) {

        stockTimers.compute(productoId, (id, viejaTarea) -> {
            if (viejaTarea != null) viejaTarea.cancel();

            TimerTask nueva = new TimerTask() {
                @Override public void run() {
                    Platform.runLater(() -> _displayNotification(titulo, texto, owner));
                }
            };
            debouncer.schedule(nueva, 500);
            return nueva;
        });
    }

    /* ----------------------- lógica interna --------------------------- */

    private boolean permitirNuevaNotificacion() {
        long now = System.currentTimeMillis();
        timestamps.removeIf(t -> now - t > TIME_WINDOW_MS);
        if (timestamps.size() >= MAX_NOTIFS_WINDOW) return false;
        timestamps.add(now);
        return true;
    }

    private void _displayNotification(String titulo, String texto, Node ownerNode) {
        Usuario usuario = GestorDeEstado.obtenerInstancia().getUsuarioActual();
        if (titulo.toLowerCase().contains("inventario") &&
                (usuario == null || !usuario.esAdmin())) {
            return; // Notificaciones de inventario solo para admins
        }

        Platform.runLater(() -> {
            try {
                Notifications builder = Notifications.create()
                        .title(titulo)
                        .text(texto)
                        .graphic(null)
                        .hideAfter(Duration.seconds(4))
                        .position(Pos.BOTTOM_LEFT);

                Window anchor = resolverOwner(ownerNode);
                if (anchor != null) builder.owner(anchor);

                if (titulo.toLowerCase().matches(".*(error|inválida).*")) {
                    builder.showError();
                } else {
                    builder.showInformation();
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error mostrando notificación", ex);
            }
        });
    }

    private Window resolverOwner(Node ownerNode) {
        if (ownerNode != null &&
                ownerNode.getScene() != null &&
                ownerNode.getScene().getWindow() != null) {
            return ownerNode.getScene().getWindow();
        }
        return Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
    }
}
