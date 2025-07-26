package recuerdo;

import modelo.diseno.ElementoDiseno;
import modelo.diseno.GrupoElementosDiseno;
import modelo.producto.Producto;
import modelo.producto.ProductoBase;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * <strong>Originador</strong> del patrón <em>Memento</em>.
 * <p>
 * Gestiona el estado completo de un {@link Producto} (incluidas sus
 * personalizaciones) y crea/recupera instantáneas mediante
 * {@link RecuerdoDiseno}.
 * </p>
 */
public class DisenoOriginador {

    private static final Logger LOGGER = Logger.getLogger(DisenoOriginador.class.getName());

    private Producto productoActual; // estado interno gestionado

    public DisenoOriginador(Producto productoInicial) {
        this.productoActual = productoInicial.clonar(); // copia defensiva
    }

    /* ------------------------------ API pública ------------------------------ */

    public void setProducto(Producto producto) {
        this.productoActual = producto.clonar();
    }

    public Producto getProducto() {
        return productoActual;
    }

    public void setElementoEnZona(String nombreZona, ElementoDiseno elemento) {
        buscarZona(nombreZona).ifPresent(z -> {
            z.limpiarElementos();
            z.anadirElemento(elemento);
        });
    }

    public void limpiarZona(String nombreZona) {
        buscarZona(nombreZona).ifPresent(GrupoElementosDiseno::limpiarElementos);
    }

    /* ------------------------------ mementos --------------------------------- */

    public RecuerdoDiseno guardarEstado() {
        LOGGER.fine(() -> "Guardando estado del producto '" + productoActual.obtenerNombre() + '\'');
        return new RecuerdoDiseno(productoActual);
    }

    public void restaurarEstado(RecuerdoDiseno recuerdo) {
        this.productoActual = recuerdo.obtenerEstadoProductoGuardado();
        LOGGER.fine(() -> "Estado del producto '" + productoActual.obtenerNombre() + "' restaurado");
    }

    /* ------------------------------ helpers ---------------------------------- */

    private Optional<GrupoElementosDiseno> buscarZona(String nombreZona) {
        if (productoActual instanceof ProductoBase base) {
            return base.obtenerElementosDiseno().stream()
                    .filter(e -> e instanceof GrupoElementosDiseno)
                    .map(e -> (GrupoElementosDiseno) e)
                    .filter(z -> z.getNombreGrupo().equals(nombreZona))
                    .findFirst();
        }
        return Optional.empty();
    }
}
