package estado;

import modelo.pedido.Pedido;
import util.AppLogic;

/**
 * Estado inicial “pendiente”: el pedido está esperando ser procesado o
 * cancelado.
 */
class EstadoPendiente implements EstadoPedido {

    @Override
    public void procesarPedido(Pedido p) {
        p.establecerEstado(AppLogic.ESTADOS.PROCESANDO);
    }

    @Override public void enviarPedido   (Pedido p) { /* no válido */ }
    @Override public void entregarPedido (Pedido p) { /* no válido */ }

    @Override
    public void cancelarPedido(Pedido p) {
        p.establecerEstado(AppLogic.ESTADOS.CANCELADO);
    }
}
