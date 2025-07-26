package comando;

import estado.ContextoEstadoPedido;
import modelo.pedido.Pedido;

/**
 * Comando que procesa un {@link Pedido}.
 */
public class ComandoProcesarPedido implements Comando {

    private final ContextoEstadoPedido ctx;
    private final Pedido pedido;
    private final String estadoAnterior;

    public ComandoProcesarPedido(ContextoEstadoPedido ctx, Pedido pedido) {
        this.ctx            = ctx;
        this.pedido         = pedido;
        this.estadoAnterior = pedido.obtenerEstado();
    }

    @Override
    public void ejecutar() {
        ctx.procesar();
    }
}
