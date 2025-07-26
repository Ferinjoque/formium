package modelo.admin;

import javafx.beans.property.*;
import modelo.producto.*;
import util.AppLogic;

/**
 * Wrapper observable que expone la información clave de un {@link Producto}
 * para ser mostrada en la tabla de la pestaña “Inventario”.
 */
public class StockProducto {

    /* ----------------------------- propiedades ----------------------------- */
    private final SimpleLongProperty    id;
    private final SimpleStringProperty  nombre;
    private final SimpleStringProperty  tipo;
    private final SimpleDoubleProperty  precio;
    private final SimpleIntegerProperty stock;

    /** Referencia al objeto de dominio original (para acciones posteriores). */
    private final Producto productoOriginal;

    /* ----------------------------- constructor ----------------------------- */

    /**
     * @param producto      instancia de dominio
     * @param stockInicial  unidades disponibles
     */
    public StockProducto(Producto producto, int stockInicial) {

        String tipoProducto    = "Producto General";
        String nombreDetallado = producto.obtenerNombre();

        if (producto instanceof ProductoCamiseta p) {
            nombreDetallado = formatearNombreDetallado(p.obtenerNombre(), p.obtenerTalla(), p.obtenerColor());
            tipoProducto    = "Camiseta";
        } else if (producto instanceof ProductoChaqueta p) {
            nombreDetallado = formatearNombreDetallado(p.obtenerNombre(), p.obtenerTalla(), p.obtenerColor());
            tipoProducto    = "Chaqueta";
        } else if (producto instanceof ProductoPantalones p) {
            nombreDetallado = formatearNombreDetallado(p.obtenerNombre(), p.obtenerTalla(), p.obtenerColor());
            tipoProducto    = "Pantalones";
        }

        ProductoBase base = (ProductoBase) producto;

        this.id     = new SimpleLongProperty(base.getId());
        this.nombre = new SimpleStringProperty(nombreDetallado);
        this.tipo   = new SimpleStringProperty(tipoProducto);
        this.precio = new SimpleDoubleProperty(base.getPrecioBase());
        this.stock  = new SimpleIntegerProperty(stockInicial);

        this.productoOriginal = producto;
    }

    /* ------------------------------ getters -------------------------------- */

    public long   getId()     { return id.get(); }
    public String getNombre() { return nombre.get(); }
    public String getTipo()   { return tipo.get(); }
    public double getPrecio() { return precio.get(); }
    public int    getStock()  { return stock.get(); }

    public SimpleLongProperty    idProperty()     { return id; }
    public SimpleStringProperty  nombreProperty() { return nombre; }
    public SimpleStringProperty  tipoProperty()   { return tipo; }
    public SimpleDoubleProperty  precioProperty() { return precio; }
    public SimpleIntegerProperty stockProperty()  { return stock; }

    public void setStock(int nuevoStock) { this.stock.set(nuevoStock); }

    public Producto getProductoOriginal() { return productoOriginal; }

    /* ---------------------------- utilidades ------------------------------- */

    /** Devuelve el nombre detallado tal cual se muestra en la tabla. */
    public static String obtenerNombreDetallado(Producto prod) {
        if (prod instanceof ProductoCamiseta p) {
            return formatearNombreDetallado(p.obtenerNombre(), p.obtenerTalla(), p.obtenerColor());
        } else if (prod instanceof ProductoChaqueta p) {
            return formatearNombreDetallado(p.obtenerNombre(), p.obtenerTalla(), p.obtenerColor());
        } else if (prod instanceof ProductoPantalones p) {
            return formatearNombreDetallado(p.obtenerNombre(), p.obtenerTalla(), p.obtenerColor());
        }
        return prod.obtenerNombre();
    }

    /* Helper privado para evitar repetición */
    private static String formatearNombreDetallado(String nombre, String talla, String color) {
        return String.format(AppLogic.FORMATOS.NOMBRE_DETALLADO_PRODUCTO, nombre, talla, color);
    }
}
