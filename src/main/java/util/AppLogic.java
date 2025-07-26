package util;

/**
 * Constantes funcionales de la aplicación.
 * <p>Cada grupo se encapsula en una inner-class estática para
 * evitar colisiones de nombres y ofrecer una semántica clara.</p>
 */
public final class AppLogic {

    private AppLogic() { }

    /* ------------------ workflow / estados de pedido ------------------ */
    public static final class ESTADOS {
        private ESTADOS() { }
        public static final String PENDIENTE   = "PENDIENTE";
        public static final String PROCESANDO  = "PROCESANDO";
        public static final String ENVIADO     = "ENVIADO";
        public static final String ENTREGADO   = "ENTREGADO";
        public static final String CANCELADO   = "CANCELADO";
    }

    /* --------------------------- observer ----------------------------- */
    public static final class OBSERVER {
        private OBSERVER() { }
        public static final String STOCK_ALERT_PREFIX = "STOCK_ALERT::";
    }

    /* ------------------------- tipos de producto ---------------------- */
    public static final class TIPOS_PRODUCTO {
        private TIPOS_PRODUCTO() { }
        public static final String CAMISETA   = "CAMISETA";
        public static final String CHAQUETA   = "CHAQUETA";
        public static final String PANTALONES = "PANTALONES";
    }

    /* ------------------------- formatos comunes ----------------------- */
    public static final class FORMATOS {
        private FORMATOS() { }
        public static final String NOMBRE_DETALLADO_PRODUCTO = "%s (%s - %s)";
    }

    /* -------------------- datos iniciales de la BD -------------------- */
    public static final class INIT_DATA {
        private INIT_DATA() { }

        /* credenciales admin */
        public static final String ADMIN_USER = "sa";
        public static final String ADMIN_PASS = "1234";

        /* stock por defecto */
        public static final int STOCK_INICIAL_CAMISETA = 5;
        public static final int STOCK_INICIAL_CHAQUETA = 10;
        public static final int STOCK_INICIAL_PANTALON = 15;

        /* ── Productos por categoría ── */
        // Camisetas
        public static final String   CAMISETA_CUELLO_REDONDO_NOMBRE  = "Camiseta Cuello Redondo";
        public static final String   CAMISETA_CUELLO_REDONDO_DESC    = "Camiseta clásica 100% algodón pima.";
        public static final double   CAMISETA_CUELLO_REDONDO_PRECIO  = 29.90;
        public static final String[] TALLAS_CAMISETA_NORMAL          = {"S", "M", "L", "XL"};
        public static final String[] COLORES_CAMISETA_NORMAL         = {"White", "Black", "Red", "Blue"};

        public static final String   CAMISETA_CUELLO_V_NOMBRE        = "Camiseta Cuello V";
        public static final String   CAMISETA_CUELLO_V_DESC          = "Camiseta con moderno cuello en V, 100% algodón pima.";
        public static final double   CAMISETA_CUELLO_V_PRECIO        = 35.90;
        public static final String[] TALLAS_CAMISETA_V               = {"S", "M", "L"};
        public static final String[] COLORES_CAMISETA_V              = {"Gray", "Green", "Navy"};

        // Chaquetas
        public static final String   CHAQUETA_JEAN_NOMBRE            = "Chaqueta de Jean";
        public static final String   CHAQUETA_JEAN_DESC              = "Chaqueta de jean clásica, un básico atemporal.";
        public static final double   CHAQUETA_JEAN_PRECIO            = 50.90;
        public static final String[] TALLAS_CHAQUETA                 = {"S", "M", "L"};
        public static final String[] MATERIALES_CHAQUETA             = {"Jean"};
        public static final String[] COLORES_CHAQUETA_JEAN           = {"Navy", "Black"};

        // Pantalones
        public static final String   PANTALON_CARGO_NOMBRE           = "Pantalones Cargo";
        public static final String   PANTALON_CARGO_DESC             = "Pantalones cargo de tela drill, cómodos y funcionales.";
        public static final double   PANTALON_CARGO_PRECIO           = 40.00;
        public static final String[] TALLAS_PANTALON                 = {"30", "32", "34", "36"};
        public static final String[] CORTES_PANTALON_CARGO           = {"Cargo"};
        public static final String[] COLORES_PANTALON_CARGO          = {"Khaki", "Olive"};

        public static final String   PANTALON_CHINO_NOMBRE           = "Pantalones Chinos";
        public static final String   PANTALON_CHINO_DESC             = "Pantalones de corte chino, look casual y elegante.";
        public static final double   PANTALON_CHINO_PRECIO           = 45.50;
        public static final String[] CORTES_PANTALON_CHINO           = {"Slim Fit"};
        public static final String[] COLORES_PANTALON_CHINO          = {"Beige", "Navy"};
    }
}
