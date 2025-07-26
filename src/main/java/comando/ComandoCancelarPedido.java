package comando;

import estado.ContextoEstadoPedido;
import modelo.pedido.Pedido;

/**
 * Comando que cancela un {@link Pedido}.
 */
public class ComandoCancelarPedido implements Comando {

    private final ContextoEstadoPedido ctx;
    private final Pedido pedido;
    private final String estadoAnterior;

    public ComandoCancelarPedido(ContextoEstadoPedido ctx, Pedido pedido) {
        this.ctx            = ctx;
        this.pedido         = pedido;
        this.estadoAnterior = pedido.obtenerEstado();
    }

    @Override
    public void ejecutar() {
        ctx.cancelar();
    }
}
