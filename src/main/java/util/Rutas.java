package util;

/**
 * Localiza todos los recursos estáticos (FXML e imágenes) en un único lugar,
 * facilitando la refactorización y evitando literales dispersos.
 */
public final class Rutas {

    private Rutas() { }

    public static final class VISTAS {
        private VISTAS() { }
        public static final String LOGIN            = "/ui/vista/login_vista.fxml";
        public static final String PRINCIPAL        = "/ui/vista/principal_vista.fxml";
        public static final String ADMIN            = "/ui/vista/admin_vista.fxml";
        public static final String PEDIDOS          = "/ui/vista/pedidos_vista.fxml";
        public static final String CHECKOUT         = "/ui/vista/checkout_vista.fxml";
        public static final String PERSONALIZACION  = "/ui/vista/personalizacion_vista.fxml";
        public static final String DETALLES_PEDIDO  = "/ui/vista/detalles_pedido_vista.fxml";
    }

    public static final class IMAGENES {
        private IMAGENES() { }
        public static final String RUTA_DISENOS      = "/ui/resources/disenos/";
        public static final String CAMISETA_BASE     = "/ui/resources/camisetaBlanca.png";
        public static final String CHAQUETA_BASE     = "/ui/resources/chaquetaBlanca.png";
        public static final String PANTALON_BASE     = "/ui/resources/pantalonBlanco.png";
        public static final String ERROR_PLACEHOLDER = "https://via.placeholder.com/100?text=Error";
    }
}
