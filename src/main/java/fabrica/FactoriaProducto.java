package fabrica;

import modelo.admin.request.CrearCamisetaRequest;
import modelo.admin.request.CrearChaquetaRequest;
import modelo.admin.request.CrearPantalonesRequest;
import modelo.producto.Producto;
import modelo.producto.ProductoCamiseta;
import modelo.producto.ProductoChaqueta;
import modelo.producto.ProductoPantalones;

/**
 * <strong>Factory Method</strong> para instanciar distintos {@link Producto}s
 * a partir de sus DTO de creación.
 *
 * <p>Se usan métodos sobrecargados para conservar un único punto de creación y
 * mantener la seguridad de tipos en cada DTO.</p>
 */
public final class FactoriaProducto {

    private FactoriaProducto() {
        /* Evita instanciación */
    }

    /** Crea una {@link ProductoCamiseta}. */
    public static Producto crearProducto(CrearCamisetaRequest req) {
        return new ProductoCamiseta(
                req.getNombre(),
                req.getPrecioBase(),
                req.getDescripcion(),
                req.getTalla(),
                req.getColor());
    }

    /** Crea una {@link ProductoChaqueta}. */
    public static Producto crearProducto(CrearChaquetaRequest req) {
        return new ProductoChaqueta(
                req.getNombre(),
                req.getPrecioBase(),
                req.getDescripcion(),
                req.getTalla(),
                req.getMaterial(),
                req.getColor());
    }

    /** Crea un {@link ProductoPantalones}. */
    public static Producto crearProducto(CrearPantalonesRequest req) {
        return new ProductoPantalones(
                req.getNombre(),
                req.getPrecioBase(),
                req.getDescripcion(),
                req.getTalla(),
                req.getCorte(),
                req.getColor());
    }
}
