package modelo.producto;

import jakarta.persistence.Entity;
import modelo.diseno.GrupoElementosDiseno;

/**
 * Variante de producto “Camiseta”.
 */
@Entity
public class ProductoCamiseta extends ProductoBase {

    private String talla;
    private String color;

    public ProductoCamiseta(String nombre, double precioBase, String descripcion,
                            String talla, String color) {
        super(nombre, precioBase, descripcion);
        this.talla = talla;
        this.color = color;
    }

    /** Constructor JPA. */
    protected ProductoCamiseta() { }

    public String obtenerTalla()  { return talla; }
    public String obtenerColor()  { return color; }

    @Override
    public Producto clonar() {
        return (ProductoCamiseta) super.clonar();
    }

    @Override
    protected void inicializarZonasDeDiseno() {
        elementosDiseno.add(
                new GrupoElementosDiseno("Frente", 0.72, 0.50, 0.45, 0.25, -40, -35)
        );
    }
}
