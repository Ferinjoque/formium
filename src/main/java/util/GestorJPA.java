package util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.logging.Logger;

/**
 * Gestor singleton del {@link EntityManagerFactory}. Encapsula la inicialización
 * y el cierre, evitando fugas de recursos.
 */
public final class GestorJPA {

    private static final Logger LOGGER = Logger.getLogger(GestorJPA.class.getName());
    private static final String PERSISTENCE_UNIT_NAME = "FormiumPU";
    private static EntityManagerFactory factory;

    private GestorJPA() { }

    /** Obtiene (o crea) la instancia de {@link EntityManagerFactory}. */
    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        return factory;
    }

    /** Cierra la factoría; debe llamarse en {@code Application.stop()}. */
    public static void shutdown() {
        if (factory != null && factory.isOpen()) {
            factory.close();
            LOGGER.info("EntityManagerFactory cerrado correctamente.");
        }
    }
}
