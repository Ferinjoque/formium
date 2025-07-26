package modelo.admin.request;

/**
 * Solicitud de creación para un producto tipo “Chaqueta”.
 */
public final class CrearChaquetaRequest extends CrearProductoRequest {

    private final String talla;
    private final String material;
    private final String color;

    public CrearChaquetaRequest(String nombre,
                                String descripcion,
                                double precioBase,
                                String talla,
                                String material,
                                String color) {
        super(nombre, descripcion, precioBase);
        this.talla    = talla;
        this.material = material;
        this.color    = color;
    }

    public String getTalla()    { return talla; }
    public String getMaterial() { return material; }
    public String getColor()    { return color; }
}
