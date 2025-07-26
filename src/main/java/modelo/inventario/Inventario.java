package modelo.inventario;

import jakarta.persistence.*;
import modelo.producto.ProductoBase;

/**
 * Entidad JPA que refleja las existencias de un {@link ProductoBase}.
 *
 * <p>Cada registro vincula de forma uno-a-uno un producto con la cantidad
 * disponible en stock.</p>
 */
@Entity
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "producto_id", unique = true, nullable = false)
    private ProductoBase producto;

    @Column(nullable = false)
    private int cantidad;

    /** Requerido por JPA. */
    protected Inventario() { }

    public Inventario(ProductoBase producto, int cantidad) {
        this.producto  = producto;
        this.cantidad  = cantidad;
    }

    /* ------------------------------ getters & setters ------------------------------ */

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public ProductoBase getProducto()            { return producto; }
    public void setProducto(ProductoBase p)      { this.producto = p; }

    public int getCantidad()                     { return cantidad; }
    public void setCantidad(int cantidad)        { this.cantidad = cantidad; }
}
