package observador;

import servicio.ServicioNotificacionesUI;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Observador especializado en logística; solo atiende alertas de stock bajo o
 * agotado.
 */
public class ObservadorLogistico implements Observador {

    private static final Logger LOGGER = Logger.getLogger(ObservadorLogistico.class.getName());

    private final String nombre;

    public ObservadorLogistico(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public void actualizar(String mensaje) {
        if (!mensaje.startsWith("STOCK_ALERT::")) return;

        LOGGER.fine(() -> "Logística (" + nombre + ") recibió: " + mensaje);

        try {
            String[] partes = mensaje.split("::", 3);
            String productoId         = partes[1];
            String textoNotificacion  = partes[2];

            if (textoNotificacion.contains("Alerta de Stock Bajo") ||
                    textoNotificacion.contains("Producto Agotado")) {
                ServicioNotificacionesUI.obtenerInstancia()
                        .notificarAlertaDeStock(productoId,
                                "Alerta de Inventario",
                                textoNotificacion,
                                null);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error al parsear mensaje de stock: " + mensaje, ex);
            ServicioNotificacionesUI.obtenerInstancia()
                    .mostrarNotificacion("Alerta de Inventario", mensaje);
        }
    }
}
