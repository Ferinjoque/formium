package modelo.admin.request;

/**
 * Solicitud de creación para un producto tipo “Camiseta”.
 */
public final class CrearCamisetaRequest extends CrearProductoRequest {

    private final String talla;
    private final String color;

    public CrearCamisetaRequest(String nombre,
                                String descripcion,
                                double precioBase,
                                String talla,
                                String color) {
        super(nombre, descripcion, precioBase);
        this.talla = talla;
        this.color = color;
    }

    public String getTalla() { return talla; }
    public String getColor() { return color; }
}
