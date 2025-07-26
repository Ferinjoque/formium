package modelo.carrito;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import modelo.producto.Producto;

import java.util.logging.Logger;

/**
 * Carrito de compras de un usuario.
 *
 * <p>Mantiene los {@link Producto}s en una {@link ObservableList}, lo que
 * permite enlazar la UI y recibir actualizaciones en tiempo real cuando se
 * añaden o eliminan ítems.</p>
 */
public class CarritoCompras {

    private static final Logger LOGGER = Logger.getLogger(CarritoCompras.class.getName());

    /** Lista observable con los productos añadidos. */
    private final ObservableList<Producto> productosEnCarrito =
            FXCollections.observableArrayList();

    /* ---------------------------------------------------------------------- */
    /*  Operaciones públicas                                                  */
    /* ---------------------------------------------------------------------- */

    /**
     * Agrega un producto al carrito.
     *
     * @param producto producto a añadir (ignorado si es {@code null})
     */
    public void anadirProducto(Producto producto) {
        if (producto == null) {
            LOGGER.fine("Intento de añadir un producto nulo al carrito.");
            return;
        }
        productosEnCarrito.add(producto);
    }

    /**
     * Elimina un producto del carrito.
     *
     * @param producto producto a eliminar (se ignora si es {@code null})
     */
    public void removerProducto(Producto producto) {
        if (producto != null) {
            productosEnCarrito.remove(producto);
        }
    }

    /** @return lista observable que la UI puede enlazar. */
    public ObservableList<Producto> obtenerProductos() {
        return productosEnCarrito;
    }

    /** @return total acumulado según {@link Producto#calcularPrecio()}. */
    public double calcularTotal() {
        return productosEnCarrito.stream()
                .mapToDouble(Producto::calcularPrecio)
                .sum();
    }

    /** Vacía por completo el carrito. */
    public void vaciarCarrito() {
        productosEnCarrito.clear();
    }
}
