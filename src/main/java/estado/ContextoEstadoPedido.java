package estado;

import modelo.pedido.Pedido;
import util.AppLogic;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contexto que mantiene el estado actual de un {@link Pedido} y delega en él
 * las operaciones de cambio de estado.
 */
public class ContextoEstadoPedido {

    private static final Logger LOGGER =
            Logger.getLogger(ContextoEstadoPedido.class.getName());

    private final ResourceBundle bundle = ResourceBundle.getBundle("ui.vista.messages");
    private final Pedido pedido;

    /** Tabla de estados conocidos indexada por su nombre lógico. */
    private final Map<String, EstadoPedido> estados = new HashMap<>();

    private EstadoPedido estadoActual;

    public ContextoEstadoPedido(Pedido pedido) {
        this.pedido = pedido;

        // Registro de todos los estados disponibles
        estados.put(AppLogic.ESTADOS.PENDIENTE,   new EstadoPendiente());
        estados.put(AppLogic.ESTADOS.PROCESANDO,  new EstadoProcesando());
        estados.put(AppLogic.ESTADOS.ENVIADO,     new EstadoEnviado());
        estados.put(AppLogic.ESTADOS.ENTREGADO,   new EstadoEntregado());
        estados.put(AppLogic.ESTADOS.CANCELADO,   new EstadoCancelado());

        establecerEstado(pedido.obtenerEstado());
    }

    /**
     * Cambia el estado actual si existe en el registro.
     *
     * @param nombre nombre lógico del estado destino
     */
    public void establecerEstado(String nombre) {
        EstadoPedido nuevo = estados.get(nombre);
        if (nuevo != null) {
            estadoActual = nuevo;
        } else {
            LOGGER.log(Level.SEVERE, "Estado de pedido no reconocido: {0}", nombre);
        }
    }

    /* ------------------- API pública ------------------- */

    public void procesar() { estadoActual.procesarPedido(pedido);  establecerEstado(pedido.obtenerEstado()); }
    public void enviar()   { estadoActual.enviarPedido(pedido);    establecerEstado(pedido.obtenerEstado()); }
    public void entregar() { estadoActual.entregarPedido(pedido);  establecerEstado(pedido.obtenerEstado()); }
    public void cancelar() { estadoActual.cancelarPedido(pedido);  establecerEstado(pedido.obtenerEstado()); }
}
