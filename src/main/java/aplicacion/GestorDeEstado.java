package aplicacion;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import modelo.carrito.CarritoCompras;
import modelo.pedido.Pedido;
import modelo.usuario.Usuario;
import servicio.ServicioPedido;

import java.util.List;
import java.util.logging.Logger;

/**
 * Singleton que centraliza el estado de la aplicación.
 *
 * <p>Se encarga de mantener:
 * <ul>
 *   <li>Un único {@link CarritoCompras} compartido entre las vistas.</li>
 *   <li>La lista observable de {@link Pedido} existentes.</li>
 *   <li>El {@link Usuario} autenticado en la sesión.</li>
 * </ul>
 * </p>
 *
 * <p>La instancia se crea de forma «lazy» y el método
 * {@link #obtenerInstancia()} está sincronizado para garantizar seguridad en
 * entornos multi-hilo.</p>
 */
public final class GestorDeEstado {

    private static final Logger LOGGER = Logger.getLogger(GestorDeEstado.class.getName());

    /** Instancia única del gestor (patrón Singleton). */
    private static GestorDeEstado instancia;

    private final CarritoCompras carrito;
    private final ObservableList<Pedido> pedidosCreados;
    private Usuario usuarioActual;

    /**
     * Constructor privado para impedir la creación directa y garantizar el
     * patrón Singleton. Inicializa el carrito y carga los pedidos ya
     * almacenados.
     */
    private GestorDeEstado() {
        this.carrito = new CarritoCompras();

        ServicioPedido servicioPedido = new ServicioPedido();
        List<Pedido> pedidos = servicioPedido.obtenerTodosLosPedidos();
        this.pedidosCreados = FXCollections.observableArrayList(pedidos);
    }

    /**
     * Devuelve la instancia única del gestor de estado.
     *
     * @return la única instancia de {@link GestorDeEstado}
     */
    public static synchronized GestorDeEstado obtenerInstancia() {
        if (instancia == null) {
            instancia = new GestorDeEstado();
        }
        return instancia;
    }

    /** @return el carrito de compras compartido */
    public CarritoCompras getCarrito() {
        return carrito;
    }

    /** @return lista observable de pedidos recuperados */
    public ObservableList<Pedido> getPedidosCreados() {
        return pedidosCreados;
    }

    /** @return usuario autenticado en la sesión */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Establece el usuario autenticado para la sesión.
     *
     * @param usuarioActual usuario autenticado
     */
    public void setUsuarioActual(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }
}
