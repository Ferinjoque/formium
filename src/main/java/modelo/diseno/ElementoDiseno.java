package modelo.diseno;

/**
 * Componente base del patrón <em>Composite</em> para elementos de diseño.
 */
public interface ElementoDiseno {

    /** @return coste adicional asociado a la personalización. */
    double obtenerPrecioAdicional();

    /**
     * Crea una copia profunda del elemento.
     *
     * @return clon independiente de la instancia original
     */
    ElementoDiseno clonar();
}
