package servicio;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import modelo.carrito.CarritoCompras;
import modelo.inventario.Inventario;
import modelo.producto.ProductoBase;
import util.GestorJPA;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lógica de acceso a datos para el módulo de inventario.
 */
public class ServicioInventario {

    private static final Logger LOGGER = Logger.getLogger(ServicioInventario.class.getName());

    /* --------------------------- consultas --------------------------- */

    public Optional<Inventario> findByProducto(ProductoBase producto) {
        EntityManager em = GestorJPA.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Inventario> q = em.createQuery(
                    "SELECT i FROM Inventario i WHERE i.producto = :prod", Inventario.class);
            q.setParameter("prod", producto);
            return Optional.of(q.getSingleResult());
        } catch (NoResultException nre) {
            return Optional.empty();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error al buscar inventario", ex);
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public List<Inventario> obtenerTodoElInventario() {
        EntityManager em = GestorJPA.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT i FROM Inventario i", Inventario.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public int obtenerStockDeProducto(ProductoBase producto) {
        return findByProducto(producto)
                .map(Inventario::getCantidad)
                .orElse(0);
    }

    public int obtenerStockDisponible(ProductoBase producto, CarritoCompras carrito) {
        long enCarrito = carrito.obtenerProductos().stream()
                .filter(p -> p instanceof ProductoBase)
                .map(p -> (ProductoBase) p)
                .filter(pb -> pb.getId().equals(producto.getId()))
                .count();
        return obtenerStockDeProducto(producto) - (int) enCarrito;
    }

    /* --------------------------- mutaciones ------------------------- */

    public void actualizarInventario(Inventario inventario) {
        ejecutarTx(em -> em.merge(inventario), "actualizar inventario");
    }

    public void crearRegistroInventario(Inventario inventario) {
        ejecutarTx(em -> em.persist(inventario), "crear inventario");
    }

    /* --------------------------- helpers ---------------------------- */

    private interface AccionTx { void apply(EntityManager em); }

    private void ejecutarTx(AccionTx accion, String descripcion) {
        EntityManager em = GestorJPA.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            accion.apply(em);
            em.getTransaction().commit();
        } catch (Exception ex) {
            rollbackSilencioso(em);
            LOGGER.log(Level.SEVERE, "Error al " + descripcion, ex);
        } finally {
            em.close();
        }
    }

    private void rollbackSilencioso(EntityManager em) {
        try {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error en rollback", ex);
        }
    }
}
