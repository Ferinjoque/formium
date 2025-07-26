package modelo.admin.request;

import java.util.Objects;

/**
 * DTO base para la creación de cualquier {@link modelo.producto.Producto}.
 * <p>Contiene los datos comunes — nombre, descripción y precio base—que
 * comparten todas las subclases específicas.</p>
 */
public class CrearProductoRequest {

    private final String nombre;
    private final String descripcion;
    private final double precioBase;

    /**
     * @param nombre       nombre comercial del producto
     * @param descripcion  descripción breve
     * @param precioBase   precio antes de personalizaciones e impuestos
     * @throws NullPointerException si {@code nombre} o {@code descripcion} son {@code null}
     */
    public CrearProductoRequest(String nombre, String descripcion, double precioBase) {
        this.nombre      = Objects.requireNonNull(nombre, "nombre");
        this.descripcion = Objects.requireNonNull(descripcion, "descripcion");
        this.precioBase  = precioBase;
    }

    public String getNombre()       { return nombre; }
    public String getDescripcion()  { return descripcion; }
    public double getPrecioBase()   { return precioBase; }
}
