package util;

import jakarta.persistence.EntityManager;
import modelo.inventario.Inventario;
import modelo.producto.ProductoBase;
import modelo.producto.ProductoCamiseta;
import modelo.producto.ProductoChaqueta;
import modelo.producto.ProductoPantalones;
import modelo.usuario.Rol;
import modelo.usuario.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pobla la base de datos con datos de ejemplo cuando se detecta que está vacía.
 * Usa valores centralizados en {@link AppLogic.INIT_DATA}.
 */
public final class InicializadorBD {

    private static final Logger LOGGER = Logger.getLogger(InicializadorBD.class.getName());

    private InicializadorBD() { }

    /** Comprueba si existen productos; si no, inserta datos de prueba. */
    public static void verificarYPoblarDatosIniciales() {
        EntityManager em = GestorJPA.getEntityManagerFactory().createEntityManager();
        try {
            long productos = em.createQuery("SELECT COUNT(p) FROM ProductoBase p", Long.class).getSingleResult();
            if (productos > 0) {
                LOGGER.fine("BD ya contiene datos — no se insertan ejemplos.");
                return;
            }

            LOGGER.info("BD vacía ⟶ cargando datos iniciales…");
            em.getTransaction().begin();

            /* ---------------- Camisetas ---------------- */
            crearVariantesCamiseta(em,
                    AppLogic.INIT_DATA.CAMISETA_CUELLO_REDONDO_NOMBRE,
                    AppLogic.INIT_DATA.CAMISETA_CUELLO_REDONDO_DESC,
                    AppLogic.INIT_DATA.CAMISETA_CUELLO_REDONDO_PRECIO,
                    AppLogic.INIT_DATA.TALLAS_CAMISETA_NORMAL,
                    AppLogic.INIT_DATA.COLORES_CAMISETA_NORMAL,
                    AppLogic.INIT_DATA.STOCK_INICIAL_CAMISETA);

            crearVariantesCamiseta(em,
                    AppLogic.INIT_DATA.CAMISETA_CUELLO_V_NOMBRE,
                    AppLogic.INIT_DATA.CAMISETA_CUELLO_V_DESC,
                    AppLogic.INIT_DATA.CAMISETA_CUELLO_V_PRECIO,
                    AppLogic.INIT_DATA.TALLAS_CAMISETA_V,
                    AppLogic.INIT_DATA.COLORES_CAMISETA_V,
                    AppLogic.INIT_DATA.STOCK_INICIAL_CAMISETA);

            /* ---------------- Chaquetas ---------------- */
            crearVariantesChaqueta(em,
                    AppLogic.INIT_DATA.CHAQUETA_JEAN_NOMBRE,
                    AppLogic.INIT_DATA.CHAQUETA_JEAN_DESC,
                    AppLogic.INIT_DATA.CHAQUETA_JEAN_PRECIO,
                    AppLogic.INIT_DATA.TALLAS_CHAQUETA,
                    AppLogic.INIT_DATA.MATERIALES_CHAQUETA,
                    AppLogic.INIT_DATA.COLORES_CHAQUETA_JEAN,
                    AppLogic.INIT_DATA.STOCK_INICIAL_CHAQUETA);

            /* ---------------- Pantalones ---------------- */
            crearVariantesPantalones(em,
                    AppLogic.INIT_DATA.PANTALON_CARGO_NOMBRE,
                    AppLogic.INIT_DATA.PANTALON_CARGO_DESC,
                    AppLogic.INIT_DATA.PANTALON_CARGO_PRECIO,
                    AppLogic.INIT_DATA.TALLAS_PANTALON,
                    AppLogic.INIT_DATA.CORTES_PANTALON_CARGO,
                    AppLogic.INIT_DATA.COLORES_PANTALON_CARGO,
                    AppLogic.INIT_DATA.STOCK_INICIAL_PANTALON);

            crearVariantesPantalones(em,
                    AppLogic.INIT_DATA.PANTALON_CHINO_NOMBRE,
                    AppLogic.INIT_DATA.PANTALON_CHINO_DESC,
                    AppLogic.INIT_DATA.PANTALON_CHINO_PRECIO,
                    AppLogic.INIT_DATA.TALLAS_PANTALON,
                    AppLogic.INIT_DATA.CORTES_PANTALON_CHINO,
                    AppLogic.INIT_DATA.COLORES_PANTALON_CHINO,
                    AppLogic.INIT_DATA.STOCK_INICIAL_PANTALON);

            /* ---------------- Usuarios por defecto ------------- */
            String hashComun = BCrypt.hashpw(AppLogic.INIT_DATA.ADMIN_PASS, BCrypt.gensalt());

            // Usuario Super Admin (sa)
            Usuario superAdminUser = new Usuario(AppLogic.INIT_DATA.ADMIN_USER, hashComun);
            superAdminUser.setRol(Rol.SUPER_ADMIN);
            em.persist(superAdminUser);

            // Usuario Administrador (admin)
            Usuario adminUser = new Usuario("admin", hashComun);
            adminUser.setRol(Rol.ADMIN);
            em.persist(adminUser);

            // Usuario Normal (user)
            Usuario normalUser = new Usuario("user", hashComun);
            normalUser.setRol(Rol.USER);
            em.persist(normalUser);


            em.getTransaction().commit();
            LOGGER.info("Datos iniciales insertados correctamente.");
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error insertando datos de prueba", ex);
        } finally {
            em.close();
        }
    }

    /* ---------------- helpers de inserción ---------------- */

    private static void crearVariantesCamiseta(EntityManager em, String nombre, String desc,
                                               double precio, String[] tallas, String[] colores, int stock) {
        for (String talla : tallas)
            for (String color : colores)
                persistir(em, new ProductoCamiseta(nombre, precio, desc, talla, color), stock);
    }

    private static void crearVariantesChaqueta(EntityManager em, String nombre, String desc,
                                               double precio, String[] tallas, String[] materiales,
                                               String[] colores, int stock) {
        for (String talla : tallas)
            for (String mat : materiales)
                for (String color : colores)
                    persistir(em, new ProductoChaqueta(nombre, precio, desc, talla, mat, color), stock);
    }

    private static void crearVariantesPantalones(EntityManager em, String nombre, String desc,
                                                 double precio, String[] tallas, String[] cortes,
                                                 String[] colores, int stock) {
        for (String talla : tallas)
            for (String corte : cortes)
                for (String color : colores)
                    persistir(em, new ProductoPantalones(nombre, precio, desc, talla, corte, color), stock);
    }

    private static void persistir(EntityManager em, ProductoBase prod, int stock) {
        em.persist(prod);
        em.persist(new Inventario(prod, stock));
    }
}