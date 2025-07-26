package modelo.producto;

import jakarta.persistence.*;
import modelo.diseno.ElementoDiseno;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad base para productos con herencia JPA (estrategia <em>JOINED</em>).
 * <p>
 * Gestiona el precio base, la descripción y las personalizaciones mediante
 * composición con {@link ElementoDiseno}.
 * </p>
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ProductoBase implements Producto, Cloneable {

    /* ----------------------------- campos JPA ----------------------------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected String nombre;
    protected double precioBase;

    @Column(length = 1000)
    protected String descripcion;

    /* ----------------------- personalizaciones en memoria ------------------ */
    @Transient
    protected List<ElementoDiseno> elementosDiseno = new ArrayList<>();

    /* ---------------------------- constructores --------------------------- */

    /** Requerido por JPA. */
    protected ProductoBase() { }

    protected ProductoBase(String nombre, double precioBase, String descripcion) {
        this.nombre       = nombre;
        this.precioBase   = precioBase;
        this.descripcion  = descripcion;
    }

    /* ----------------------------- getters/setters ------------------------ */

    public Long getId()                  { return id; }
    public void setId(Long id)           { this.id = id; }

    public double getPrecioBase()        { return precioBase; }
    public void setPrecioBase(double p)  { this.precioBase = p; }

    public void setDescripcion(String d) { this.descripcion = d; }

    /* ------------------------ gestión de personalización ------------------ */

    /** Añade un elemento de diseño al producto. */
    public void anadirElementoDiseno(ElementoDiseno elemento) {
        elementosDiseno.add(elemento);
    }

    /** Elimina todas las personalizaciones actuales. */
    public void limpiarElementosDiseno() {
        elementosDiseno.clear();
    }

    /** Copia defensiva de las personalizaciones. */
    public List<ElementoDiseno> obtenerElementosDiseno() {
        return new ArrayList<>(elementosDiseno);
    }

    /* ------------------------------ Producto ------------------------------ */

    @Override
    public String obtenerNombre() {
        return nombre;
    }

    @Override
    public String obtenerDescripcion() {
        return descripcion;
    }

    @Override
    public double calcularPrecio() {
        double total = precioBase;
        for (ElementoDiseno elemento : elementosDiseno) {
            total += elemento.obtenerPrecioAdicional();
        }
        return total;
    }

    @Override
    public Producto clonar() {
        try {
            ProductoBase copia = (ProductoBase) super.clone();
            copia.elementosDiseno = new ArrayList<>();
            for (ElementoDiseno ed : elementosDiseno) {
                copia.elementosDiseno.add(ed.clonar());
            }
            return copia;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError("Clonación no soportada", ex);
        }
    }

    /* ----------------------------- JPA hooks ------------------------------ */

    /** Inicializa las zonas de diseño tras la carga de la entidad. */
    @PostLoad
    protected void onPostLoad() {
        elementosDiseno.clear();
        inicializarZonasDeDiseno();
    }

    /* -------------------------- plantilla de subclase --------------------- */

    /** Define las zonas de diseño por defecto para cada tipo concreto. */
    protected abstract void inicializarZonasDeDiseno();
}
