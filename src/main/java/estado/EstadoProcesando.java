package estado;

import modelo.pedido.Pedido;
import util.AppLogic;

/**
 * Estado “procesando”: pedido en preparación; puede enviarse o cancelarse.
 */
class EstadoProcesando implements EstadoPedido {

    @Override public void procesarPedido (Pedido p) { /* ya procesando */ }

    @Override
    public void enviarPedido(Pedido p) {
        p.establecerEstado(AppLogic.ESTADOS.ENVIADO);
    }

    @Override public void entregarPedido (Pedido p) { /* no válido */ }

    @Override
    public void cancelarPedido(Pedido p) {
        p.establecerEstado(AppLogic.ESTADOS.CANCELADO);
    }
}
