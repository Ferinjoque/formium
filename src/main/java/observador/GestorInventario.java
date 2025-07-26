package observador;

import modelo.admin.StockProducto;
import modelo.producto.Producto;
import modelo.producto.ProductoBase;
import servicio.ServicioInventario;
import util.AppLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Publisher que centraliza las operaciones de stock e informa a los
 * {@link Observador}s suscritos cuando se producen eventos relevantes.
 *
 * <p>Implementa el patrón Singleton.</p>
 */
public final class GestorInventario implements Sujeto {

    /* --------------------------- Singleton --------------------------- */
    private static GestorInventario instancia;
    public static synchronized GestorInventario obtenerInstancia() {
        if (instancia == null) instancia = new GestorInventario();
        return instancia;
    }

    /* --------------------------- atributos --------------------------- */
    private static final Logger LOGGER = Logger.getLogger(GestorInventario.class.getName());

    private final List<Observador> observadores = new ArrayList<>();
    private final ServicioInventario servicioInventario = new ServicioInventario();
    private final ResourceBundle bundle = ResourceBundle.getBundle("ui.vista.messages");

    private static final int UMBRAL_STOCK_BAJO = 3;

    private GestorInventario() { }

    /* -------------------- gestión de observadores ------------------- */
    @Override public void anadirObservador(Observador o) { observadores.add(o); }

    /* --------------------------- utilidades -------------------------- */
    private void notificarObservadores(String texto, Producto prod) {
        String msg = String.format("%s%s::%s",
                AppLogic.OBSERVER.STOCK_ALERT_PREFIX,
                ((ProductoBase) prod).getId(),
                texto);
        observadores.forEach(o -> o.actualizar(msg));
    }

    /* ---------------------- operaciones de stock -------------------- */

    public void venderProducto(Producto prod, int cantidad) {
        servicioInventario.findByProducto((ProductoBase) prod).ifPresent(inv -> {
            int stockActual = inv.getCantidad();
            if (stockActual >= cantidad) {
                int nuevoStock = stockActual - cantidad;
                inv.setCantidad(nuevoStock);
                servicioInventario.actualizarInventario(inv);

                LOGGER.info(() -> "Venta registrada: " + cantidad + " de " + prod.obtenerNombre());

                String nombreDetallado = StockProducto.obtenerNombreDetallado(prod);

                if (nuevoStock <= UMBRAL_STOCK_BAJO && nuevoStock > 0) {
                    notificarObservadores(
                            String.format(bundle.getString("notif.stock.baja.msg"), nombreDetallado, nuevoStock),
                            prod);
                } else if (nuevoStock == 0) {
                    notificarObservadores(
                            String.format(bundle.getString("notif.stock.agotado.msg"), nombreDetallado),
                            prod);
                }
            } else {
                notificarObservadores(
                        String.format(bundle.getString("notif.stock.fallo_venta.msg"),
                                StockProducto.obtenerNombreDetallado(prod)),
                        prod);
            }
        });
    }

    public void reponerStock(Producto prod, int cantidad) {
        servicioInventario.findByProducto((ProductoBase) prod).ifPresent(inv -> {
            int nuevo = inv.getCantidad() + cantidad;
            inv.setCantidad(nuevo);
            servicioInventario.actualizarInventario(inv);

            LOGGER.info(() -> "Stock repuesto para " + prod.obtenerNombre());

            String nombreDetallado = StockProducto.obtenerNombreDetallado(prod);
            notificarObservadores(
                    String.format(bundle.getString("notif.stock.normalizado.msg"), nombreDetallado, nuevo),
                    prod);
        });
    }
}
