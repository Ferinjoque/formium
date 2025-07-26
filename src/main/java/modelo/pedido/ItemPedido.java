package modelo.pedido;

import jakarta.persistence.*;
import modelo.producto.ProductoBase;

/**
 * Ítem individual dentro de un {@link Pedido}.
 * <p>Almacena la cantidad, el precio unitario al momento de la compra y la
 * personalización aplicada (en formato JSON).</p>
 */
@Entity
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Muchos ítems pueden referenciar al mismo producto del catálogo. */
    @ManyToOne(optional = false)
    private ProductoBase productoBase;

    /** Pedido contenedor (relación bidireccional). */
    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    private int    cantidad;
    private double precioUnitario;

    /** JSON con los detalles de personalización. */
    @Lob
    @Column(length = 2048)
    private String personalizacionJson;

    /** Constructor por defecto requerido por JPA. */
    public ItemPedido() { }

    /* ------------------------------ getters & setters ------------------------------ */

    public Long getId()                           { return id; }
    public void setId(Long id)                    { this.id = id; }

    public ProductoBase getProductoBase()         { return productoBase; }
    public void setProductoBase(ProductoBase pb)  { this.productoBase = pb; }

    public Pedido getPedido()                     { return pedido; }
    public void setPedido(Pedido pedido)          { this.pedido = pedido; }

    public int getCantidad()                      { return cantidad; }
    public void setCantidad(int cantidad)         { this.cantidad = cantidad; }

    public double getPrecioUnitario()             { return precioUnitario; }
    public void setPrecioUnitario(double precio)  { this.precioUnitario = precio; }

    public String getPersonalizacionJson()        { return personalizacionJson; }
    public void setPersonalizacionJson(String js) { this.personalizacionJson = js; }
}
