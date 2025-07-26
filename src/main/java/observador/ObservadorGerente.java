package observador;

import servicio.ServicioNotificacionesUI;

import java.util.logging.Logger;

/**
 * Observador que representa a un gerente y recibe notificaciones generales.
 */
public class ObservadorGerente implements Observador {

    private static final Logger LOGGER = Logger.getLogger(ObservadorGerente.class.getName());

    private final String nombre;

    public ObservadorGerente(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public void actualizar(String mensaje) {
        LOGGER.fine(() -> "Gerente (" + nombre + ") recibió: " + mensaje);

        if (mensaje.startsWith("STOCK_ALERT::")) {
            despacharAlertaStock(mensaje);
        } else if (!mensaje.contains("Fallo de venta")) {
            ServicioNotificacionesUI.obtenerInstancia()
                    .mostrarNotificacion("Notificación General", mensaje);
        }
    }

    private void despacharAlertaStock(String mensaje) {
        try {
            String textoNotificacion = mensaje.split("::", 3)[2];

            // Solo las reposiciones interesan al gerente
            if (textoNotificacion.contains("Repuesto")) {
                ServicioNotificacionesUI.obtenerInstancia()
                        .mostrarNotificacion("Notificación General", textoNotificacion);
            }
        } catch (Exception ex) {
            LOGGER.warning("Error al parsear mensaje de stock: " + mensaje);
            ServicioNotificacionesUI.obtenerInstancia()
                    .mostrarNotificacion("Notificación General", mensaje);
        }
    }
}
