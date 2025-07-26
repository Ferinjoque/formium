package comando;

import estado.ContextoEstadoPedido;
import modelo.pedido.Pedido;

/**
 * Comando que marca un {@link Pedido} como entregado.
 */
public class ComandoEntregarPedido implements Comando {

    private final ContextoEstadoPedido ctx;
    private final Pedido pedido;
    private final String estadoAnterior;

    public ComandoEntregarPedido(ContextoEstadoPedido ctx, Pedido pedido) {
        this.ctx            = ctx;
        this.pedido         = pedido;
        this.estadoAnterior = pedido.obtenerEstado();
    }

    @Override
    public void ejecutar() {
        ctx.entregar();
    }
}
