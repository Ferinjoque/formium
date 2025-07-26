package modelo.producto;

import jakarta.persistence.Entity;
import modelo.diseno.GrupoElementosDiseno;

/**
 * Variante de producto “Pantalones”.
 */
@Entity
public class ProductoPantalones extends ProductoBase {

    private String talla;
    private String corte;
    private String color;

    public ProductoPantalones(String nombre, double precioBase, String descripcion,
                              String talla, String corte, String color) {
        super(nombre, precioBase, descripcion);
        this.talla = talla;
        this.corte = corte;
        this.color = color;
    }

    protected ProductoPantalones() { }

    public String obtenerTalla()  { return talla; }
    public String obtenerCorte()  { return corte; }
    public String obtenerColor()  { return color; }

    @Override
    public Producto clonar() {
        return (ProductoPantalones) super.clonar();
    }

    @Override
    protected void inicializarZonasDeDiseno() {
        elementosDiseno.add(
                new GrupoElementosDiseno("Muslo Derecho", 1.36, 0.60, 0.20, 0.35, -141, -15, 0.7)
        );
    }
}
