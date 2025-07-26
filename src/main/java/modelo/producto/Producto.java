package modelo.producto;

/**
 * Comportamiento común de cualquier artículo vendible en la tienda.
 */
public interface Producto {

    /** @return nombre del producto (variante específica en caso de haberla). */
    String obtenerNombre();

    /** @return precio final, incluida cualquier personalización. */
    double calcularPrecio();

    /** @return descripción comercial. */
    String obtenerDescripcion();

    /**
     * Devuelve una copia profunda de la instancia.
     *
     * @throws AssertionError si la subclase no admite clonación
     */
    Producto clonar();
}
