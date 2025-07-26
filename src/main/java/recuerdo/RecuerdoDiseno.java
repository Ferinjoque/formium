package recuerdo;

import modelo.producto.Producto;

/**
 * <strong>Memento</strong> que almacena una instantánea completa de un
 * {@link Producto}.
 */
public class RecuerdoDiseno {

    private final Producto estadoProductoGuardado; // copia profunda

    public RecuerdoDiseno(Producto productoAClonar) {
        this.estadoProductoGuardado = productoAClonar.clonar();
    }

    /**
     * Devuelve un clon adicional para preservar la inmutabilidad del memento.
     */
    public Producto obtenerEstadoProductoGuardado() {
        return estadoProductoGuardado.clonar();
    }
}
