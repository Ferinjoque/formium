package modelo.diseno;

/**
 * Hoja concreta del <em>Composite</em> que representa una imagen.
 */
public class ElementoImagen implements ElementoDiseno {

    private final String urlImagen;
    private final String descripcion;
    private final double precioExtra;

    public ElementoImagen(String urlImagen, String descripcion, double precioExtra) {
        this.urlImagen   = urlImagen;
        this.descripcion = descripcion;
        this.precioExtra = precioExtra;
    }

    public String getUrlImagen()   { return urlImagen; }
    public String getDescripcion() { return descripcion; }

    @Override
    public double obtenerPrecioAdicional() {
        return precioExtra;
    }

    @Override
    public ElementoDiseno clonar() {
        return new ElementoImagen(urlImagen, descripcion, precioExtra);
    }
}
