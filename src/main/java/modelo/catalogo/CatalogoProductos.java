package modelo.catalogo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import modelo.inventario.Inventario;
import modelo.producto.Producto;
import modelo.producto.ProductoBase;
import util.GestorJPA;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton responsable de la persistencia y gestión del catálogo de
 * {@link Producto}s.
 */
public final class CatalogoProductos {

    private static final Logger LOGGER = Logger.getLogger(CatalogoProductos.class.getName());

    private static CatalogoProductos instancia;

    private CatalogoProductos() {
        /* Constructor privado: patrón Singleton */
    }

    /** @return instancia única del catálogo. */
    public static synchronized CatalogoProductos obtenerInstancia() {
        if (instancia == null) {
            instancia = new CatalogoProductos();
        }
        return instancia;
    }

    /* ---------------------------------------------------------------------- */
    /*  CRUD básicos                                                          */
    /* ---------------------------------------------------------------------- */

    /**
     * Persiste o actualiza un producto.
     *
     * @param producto producto a guardar
     * @return instancia gestionada resultante o {@code null} si ocurre error
     */
    public Producto guardarProducto(Producto producto) {
        EntityManager em = GestorJPA.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Producto productoGuardado = em.merge(producto); // devuelve la instancia gestionada
            em.getTransaction().commit();
            return productoGuardado;
        } catch (Exception e) {
            rollbackSilencioso(em);
            LOGGER.log(Level.SEVERE, "Error al guardar producto", e);
            return null;
        } finally {
            em.close();
        }
    }

    /** @return todos los productos registrados en la base de datos. */
    public List<Producto> obtenerTodosLosProductos() {
        EntityManager em = GestorJPA.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT p FROM ProductoBase p", Producto.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Elimina un producto (y su inventario asociado) de la base de datos.
     *
     * @param producto producto a borrar
     */
    public void eliminarProducto(Producto producto) {
        if (producto == null || ((ProductoBase) producto).getId() == null) {
            LOGGER.warning("No se puede eliminar un producto nulo o no persistido.");
            return;
        }

        EntityManager em = GestorJPA.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            /* --- Eliminar inventario asociado (si existiera) --- */
            TypedQuery<Inventario> qInv = em.createQuery(
                    "SELECT i FROM Inventario i WHERE i.producto.id = :productoId", Inventario.class);
            qInv.setParameter("productoId", ((ProductoBase) producto).getId());

            try {
                Inventario inv = qInv.getSingleResult();
                em.remove(inv);
            } catch (NoResultException nre) {
                LOGGER.log(Level.INFO,
                        "Sin inventario asociado para producto ID {0}. Se eliminará solo el producto.",
                        ((ProductoBase) producto).getId());
            }

            /* --- Eliminar el propio producto --- */
            Producto prodManaged = em.find(ProductoBase.class, ((ProductoBase) producto).getId());
            if (prodManaged != null) {
                em.remove(prodManaged);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            rollbackSilencioso(em);
            LOGGER.log(Level.SEVERE, "Error al eliminar producto", e);
        } finally {
            em.close();
        }
    }

    /**
     * Devuelve todas las variantes de un producto que comparten el mismo nombre.
     *
     * @param nombre nombre del producto base
     * @return lista de variantes
     */
    public List<ProductoBase> encontrarVariantesPorNombre(String nombre) {
        EntityManager em = GestorJPA.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ProductoBase> query = em.createQuery(
                    "SELECT p FROM ProductoBase p WHERE p.nombre = :nombre", ProductoBase.class);
            query.setParameter("nombre", nombre);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /* ---------------------------------------------------------------------- */
    /*  Helpers                                                               */
    /* ---------------------------------------------------------------------- */

    private void rollbackSilencioso(EntityManager em) {
        try {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } catch (Exception rollbackEx) {
            LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
        }
    }
}
