package estado;

import modelo.pedido.Pedido;

/**
 * Estado “cancelado”: un pedido ya no admite más transiciones.
 */
class EstadoCancelado implements EstadoPedido {

    @Override public void procesarPedido (Pedido p) { /* sin efecto */ }
    @Override public void enviarPedido   (Pedido p) { /* sin efecto */ }
    @Override public void entregarPedido (Pedido p) { /* sin efecto */ }
    @Override public void cancelarPedido (Pedido p) { /* sin efecto */ }
}