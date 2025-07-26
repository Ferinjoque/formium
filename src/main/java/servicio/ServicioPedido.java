package servicio;

import jakarta.persistence.EntityManager;
import modelo.pedido.Pedido;
import org.hibernate.Hibernate;
import util.GestorJPA;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Acceso a datos y operaciones de dominio para {@link Pedido}.
 */
public class ServicioPedido {

    private static final Logger LOGGER = Logger.getLogger(ServicioPedido.class.getName());

    /* ----------------------------- consultas ----------------------------- */

    public List<Pedido> obtenerTodosLosPedidos() {
        EntityManager em = GestorJPA.getEntityManagerFactory().createEntityManager();
        try {
            List<Pedido> pedidos = em.createQuery("SELECT p FROM Pedido p", Pedido.class)
                    .getResultList();
            pedidos.forEach(p -> Hibernate.initialize(p.getItems())); // evitar lazy issues
            return pedidos;
        } finally {
            em.close();
        }
    }

    /* ----------------------------- mutaciones --------------------------- */

    public void actualizarPedido(Pedido pedido) {
        ejecutarTx(em -> em.merge(pedido), "actualizar pedido");
    }

    public Pedido guardarNuevoPedido(Pedido pedido) {
        ejecutarTx(em -> em.persist(pedido), "guardar nuevo pedido");
        return pedido;
    }

    /* ----------------------------- helpers ------------------------------ */

    private interface AccionTx { void apply(EntityManager em); }

    private void ejecutarTx(AccionTx accion, String desc) {
        EntityManager em = GestorJPA.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            accion.apply(em);
            em.getTransaction().commit();
        } catch (Exception ex) {
            rollbackSilencioso(em);
            LOGGER.log(Level.SEVERE, "Error al " + desc, ex);
            throw new RuntimeException("Error al " + desc, ex);
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
