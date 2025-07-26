package servicio;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import modelo.usuario.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import util.GestorJPA;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestión de usuarios: registro, autenticación y CRUD básico.
 */
public class ServicioUsuario {

    private static final Logger LOGGER = Logger.getLogger(ServicioUsuario.class.getName());

    /* -------------------------- registro & login -------------------------- */

    public Usuario registrarUsuario(String nombreUsuario, String password) {
        if (buscarPorNombreUsuario(nombreUsuario).isPresent()) {
            LOGGER.info(() -> "Usuario ya existe: " + nombreUsuario);
            return null;
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        Usuario nuevo = new Usuario(nombreUsuario, hash);
        persistir(nuevo, "registrar usuario");
        return nuevo;
    }

    public Optional<Usuario> autenticarUsuario(String nombreUsuario, String password) {
        return buscarPorNombreUsuario(nombreUsuario)
                .filter(u -> BCrypt.checkpw(password, u.getPasswordHash()));
    }

    /* -------------------------- búsquedas ------------------------------- */

    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        EntityManager em = GestorJPA.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Usuario> q = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.nombreUsuario = :nombre", Usuario.class);
            q.setParameter("nombre", nombreUsuario);
            return Optional.of(q.getSingleResult());
        } catch (NoResultException nre) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public List<Usuario> obtenerTodosLosUsuarios() {
        EntityManager em = GestorJPA.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList();
        } finally {
            em.close();
        }
    }

    /* -------------------------- mutaciones ------------------------------ */

    public void actualizarUsuario(Usuario usuario) {
        persistir(usuario, "actualizar usuario");
    }

    public void eliminarUsuario(Usuario usuario) {
        if (usuario == null) return;
        ejecutarTx(em -> {
            Usuario managed = em.find(Usuario.class, usuario.getId());
            if (managed != null) em.remove(managed);
        }, "eliminar usuario");
    }

    /* -------------------------- helpers TX ------------------------------ */

    private interface AccionTx { void apply(EntityManager em); }

    private void persistir(Usuario usuario, String desc) {
        ejecutarTx(em -> em.merge(usuario), desc);
    }

    private void ejecutarTx(AccionTx accion, String desc) {
        EntityManager em = GestorJPA.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            accion.apply(em);
            em.getTransaction().commit();
        } catch (Exception ex) {
            rollbackSilencioso(em);
            LOGGER.log(Level.SEVERE, "Error al " + desc, ex);
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

    /* -------------------------- utilidades ------------------------------ */

    public String hashearPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
