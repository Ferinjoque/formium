package modelo.producto;

import jakarta.persistence.Entity;
import modelo.diseno.GrupoElementosDiseno;

/**
 * Variante de producto “Chaqueta”.
 */
@Entity
public class ProductoChaqueta extends ProductoBase {

    private String talla;
    private String material;
    private String color;

    public ProductoChaqueta(String nombre, double precioBase, String descripcion,
                            String talla, String material, String color) {
        super(nombre, precioBase, descripcion);
        this.talla    = talla;
        this.material = material;
        this.color    = color;
    }

    protected ProductoChaqueta() { }

    public String obtenerTalla()     { return talla; }
    public String obtenerMaterial()  { return material; }
    public String obtenerColor()     { return color; }

    @Override
    public Producto clonar() {
        return (ProductoChaqueta) super.clonar();
    }

    @Override
    protected void inicializarZonasDeDiseno() {
        elementosDiseno.add(
                new GrupoElementosDiseno("Pecho Izquierdo", 0.48, 0.56, 0.22, 0.22, -14, -37)
        );
    }
}
