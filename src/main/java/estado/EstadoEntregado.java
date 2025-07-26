package estado;

import modelo.pedido.Pedido;

/**
 * Estado “entregado”: fin del ciclo de vida del pedido.
 */
class EstadoEntregado implements EstadoPedido {

    @Override public void procesarPedido (Pedido p) { /* sin efecto */ }
    @Override public void enviarPedido   (Pedido p) { /* sin efecto */ }
    @Override public void entregarPedido (Pedido p) { /* sin efecto */ }
    @Override public void cancelarPedido (Pedido p) { /* sin efecto */ }
}
