package ui.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.Rutas;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador raíz de la vista de administración.
 * <p>Conecta las sub-pestañas y permite volver a la vista principal.</p>
 */
public class AdminControlador implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AdminControlador.class.getName());

    /* --------------- sub-controladores inyectados desde el FXML --------------- */
    @FXML private AdminPedidosControlador    pedidosTabController;
    @FXML private AdminUsuariosControlador   usuariosTabController;
    @FXML private AdminProductosControlador  productosTabController;
    @FXML private AdminInventarioControlador inventarioTabController;

    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bundle = resourceBundle;

        /* permite que otras pestañas refresquen el inventario tras sus cambios */
        if (pedidosTabController   != null && inventarioTabController != null)
            pedidosTabController.setAdminInventarioControlador(inventarioTabController);

        if (productosTabController != null && inventarioTabController != null)
            productosTabController.setAdminInventarioControlador(inventarioTabController);
    }

    /** Vuelve a la ventana principal. */
    @FXML
    void regresarPrincipal(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(Rutas.VISTAS.PRINCIPAL), bundle);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1_000, 700));
            stage.setTitle(bundle.getString("window.title.principal"));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al cargar la vista principal", ex);
        }
    }
}
