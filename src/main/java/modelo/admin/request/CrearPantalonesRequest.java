package modelo.admin.request;

/**
 * Solicitud de creación para un producto tipo “Pantalones”.
 */
public final class CrearPantalonesRequest extends CrearProductoRequest {

    private final String talla;
    private final String corte;
    private final String color;

    public CrearPantalonesRequest(String nombre,
                                  String descripcion,
                                  double precioBase,
                                  String talla,
                                  String corte,
                                  String color) {
        super(nombre, descripcion, precioBase);
        this.talla = talla;
        this.corte = corte;
        this.color = color;
    }

    public String getTalla() { return talla; }
    public String getCorte() { return corte; }
    public String getColor() { return color; }
}
