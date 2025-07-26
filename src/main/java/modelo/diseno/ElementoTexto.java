package modelo.diseno;

/**
 * Hoja concreta del <em>Composite</em> que representa un texto.
 */
public class ElementoTexto implements ElementoDiseno {

    private final String texto;
    private final String fuente;
    private final String color;
    private final double precioExtra;

    public ElementoTexto(String texto, String fuente, String color, double precioExtra) {
        this.texto       = texto;
        this.fuente      = fuente;
        this.color       = color;
        this.precioExtra = precioExtra;
    }

    public String getTexto()  { return texto; }
    public String getFuente() { return fuente; }
    public String getColor()  { return color; }

    @Override
    public double obtenerPrecioAdicional() {
        return precioExtra;
    }

    @Override
    public ElementoDiseno clonar() {
        return new ElementoTexto(texto, fuente, color, precioExtra);
    }
}
