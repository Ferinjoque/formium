package estado;

import modelo.pedido.Pedido;
import util.AppLogic;

/**
 * Estado “enviado”: el pedido viaja al cliente y puede pasar a “entregado”.
 */
class EstadoEnviado implements EstadoPedido {

    @Override public void procesarPedido (Pedido p) { /* sin efecto */ }
    @Override public void enviarPedido   (Pedido p) { /* ya enviado */ }

    @Override
    public void entregarPedido(Pedido p) {
        p.establecerEstado(AppLogic.ESTADOS.ENTREGADO);
    }

    @Override public void cancelarPedido (Pedido p) { /* sin efecto */ }
}
