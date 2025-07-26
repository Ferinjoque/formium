package comando;

import estado.ContextoEstadoPedido;
import modelo.pedido.Pedido;

/**
 * Comando que envía un {@link Pedido}.
 */
public class ComandoEnviarPedido implements Comando {

    private final ContextoEstadoPedido ctx;
    private final Pedido pedido;
    private final String estadoAnterior;

    public ComandoEnviarPedido(ContextoEstadoPedido ctx, Pedido pedido) {
        this.ctx            = ctx;
        this.pedido         = pedido;
        this.estadoAnterior = pedido.obtenerEstado();
    }

    @Override
    public void ejecutar() {
        ctx.enviar();
    }
}
