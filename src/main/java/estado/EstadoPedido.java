package estado;

import modelo.pedido.Pedido;

/**
 * Contrato del patrón <em>State</em> para {@link Pedido}.
 *
 * <p>Cada implementación representa un estado concreto y define qué
 * transiciones son válidas desde él.</p>
 */
public interface EstadoPedido {

    /** Intenta pasar el pedido a “procesando”. */
    void procesarPedido(Pedido pedido);

    /** Intenta pasar el pedido a “enviado”. */
    void enviarPedido(Pedido pedido);

    /** Intenta pasar el pedido a “entregado”. */
    void entregarPedido(Pedido pedido);

    /** Intenta cancelar el pedido. */
    void cancelarPedido(Pedido pedido);
}
