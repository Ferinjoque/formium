package comando;

/**
 * Abstracción del patrón <em>Command</em>.
 * <p>
 * Cada implementación encapsula una acción concreta que puede ejecutarse sobre
 * el dominio de la aplicación.
 * </p>
 */
@FunctionalInterface
public interface Comando {

    /** Ejecuta la operación encapsulada. */
    void ejecutar();
}
