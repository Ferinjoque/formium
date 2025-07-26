package comando;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Invocador del patrón <em>Command</em>.
 * <p>
 * Ejecuta comandos y lleva un historial para permitir futuras operaciones de
 * deshacer (no implementadas). Utiliza {@link java.util.logging.Logger}
 * para registrar posibles fallos de ejecución.
 * </p>
 */
public class InvocadorComando {

    private static final Logger LOGGER = Logger.getLogger(InvocadorComando.class.getName());

    /** Historial LIFO de comandos ejecutados. */
    private final Deque<Comando> historial = new ArrayDeque<>();

    /**
     * Ejecuta el comando indicado y lo almacena en el historial.
     *
     * @param comando instancia concreta de {@link Comando}
     */
    public void ejecutarComando(Comando comando) {
        try {
            comando.ejecutar();
            historial.push(comando);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al ejecutar comando: {0}", comando.getClass().getSimpleName());
            LOGGER.log(Level.SEVERE, "Traza de la excepción", ex);
            // Decisión de negocio: no relanzamos para evitar detener el flujo de la UI.
        }
    }
}
