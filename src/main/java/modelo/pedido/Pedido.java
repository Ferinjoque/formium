package modelo.pedido;

import jakarta.persistence.*;
import modelo.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un pedido de compra realizado por un {@link Usuario}.
 *
 * <p>Se construye mediante el patrón <em>Builder</em> (ver clase interna
 * {@link ConstructorPedido}).</p>
 */
@Entity
public class Pedido {

    /* ------------------------------ atributos ------------------------------------ */

    @Id
    private String idPedido;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "pedido")
    private List<ItemPedido> items = new ArrayList<>();

    private String        direccionEnvio;
    private String        metodoPago;
    private double        costoTotal;
    private LocalDateTime fechaCreacion;
    private String        estado;

    /* ------------------------------ constructores -------------------------------- */

    /** Requerido por JPA. */
    protected Pedido() { }

    /**
     * Añade un ítem y establece la relación inversa.
     */
    public void addItem(ItemPedido item) {
        items.add(item);
        item.setPedido(this);
    }

    /* Constructor privado usado por el builder */
    private Pedido(String idPedido, Usuario usuario,
                   String direccionEnvio, String metodoPago) {
        this.idPedido      = idPedido;
        this.usuario       = usuario;
        this.direccionEnvio = direccionEnvio;
        this.metodoPago    = metodoPago;
        this.fechaCreacion = LocalDateTime.now();
        this.estado        = "PENDIENTE";
        this.costoTotal    = 0;
    }

    /* ------------------------------ getters -------------------------------------- */

    public String   obtenerIdPedido()      { return idPedido; }
    public Usuario  getUsuario()           { return usuario; }
    public String   obtenerEstado()        { return estado; }
    public double   obtenerCostoTotal()    { return costoTotal; }
    public List<ItemPedido> getItems()     { return items; }
    public LocalDateTime obtenerFechaCreacion() { return fechaCreacion; }

    /* ------------------------------ setters -------------------------------------- */

    public void setUsuario(Usuario u)             { this.usuario = u; }
    public void establecerEstado(String estado)   { this.estado = estado; }

    /* ------------------------------ lógica --------------------------------------- */

    /** Recalcula el coste total en función de los ítems actuales. */
    public void recalcularCostoTotal() {
        costoTotal = items.stream()
                .mapToDouble(i -> i.getPrecioUnitario() * i.getCantidad())
                .sum();
    }

    /* ------------------------------ Builder -------------------------------------- */

    /**
     * Builder para instanciar {@link Pedido}s de forma controlada.
     */
    public static class ConstructorPedido {

        private final Pedido pedido;

        public ConstructorPedido(Usuario usuario, String idPedido) {
            if (usuario == null) {
                throw new IllegalArgumentException("El usuario no puede ser nulo para crear un pedido.");
            }
            this.pedido = new Pedido(idPedido, usuario, "", "");
        }

        public ConstructorPedido anadirItem(ItemPedido item) {
            this.pedido.addItem(item);
            return this;
        }

        public ConstructorPedido establecerDireccionEnvio(String dir) {
            this.pedido.direccionEnvio = dir;
            return this;
        }

        public ConstructorPedido establecerMetodoPago(String metodo) {
            this.pedido.metodoPago = metodo;
            return this;
        }

        /** Valida los datos y devuelve la instancia construida. */
        public Pedido construir() {
            if (pedido.usuario == null ||
                    pedido.items.isEmpty() ||
                    pedido.direccionEnvio.isEmpty() ||
                    pedido.metodoPago.isEmpty()) {
                throw new IllegalStateException("Faltan datos para crear el pedido.");
            }
            pedido.recalcularCostoTotal();
            return pedido;
        }
    }
}
