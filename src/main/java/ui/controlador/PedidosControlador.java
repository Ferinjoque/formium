package ui.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.pedido.Pedido;
import modelo.usuario.Usuario;
import util.Rutas;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Muestra los pedidos del usuario autenticado y permite ver su detalle.
 */
public class PedidosControlador implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(PedidosControlador.class.getName());

    /* ----------------------------- FXML ----------------------------- */
    @FXML private ListView<Pedido> listaPedidos;

    /* --------------------------- recursos --------------------------- */
    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bundle = resourceBundle;

        VBox.setVgrow(listaPedidos, Priority.ALWAYS);

        listaPedidos.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Pedido item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : bundle.getString("pedidos.list.item")
                        .formatted(item.obtenerIdPedido(), item.obtenerEstado(), item.obtenerCostoTotal()));
            }
        });

        listaPedidos.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2) {
                abrirVentanaDeDetalles(listaPedidos.getSelectionModel().getSelectedItem());
            }
        });
    }

    /**
     * Filtra los pedidos para el usuario y los muestra.
     */
    public void setPedidos(ObservableList<Pedido> todos, Usuario usuario) {
        if (usuario == null) return;
        List<Pedido> propios = todos.stream()
                .filter(p -> p.getUsuario() != null &&
                        p.getUsuario().getId().equals(usuario.getId()))
                .collect(Collectors.toList());
        listaPedidos.setItems(FXCollections.observableArrayList(propios));
    }

    /* ------------------------- navegación -------------------------- */

    private void abrirVentanaDeDetalles(Pedido pedido) {
        if (pedido == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Rutas.VISTAS.DETALLES_PEDIDO), bundle);
            Parent root = loader.load();

            DetallesPedidoControlador ctrl = loader.getController();
            ctrl.setPedido(pedido);

            Stage st = new Stage();
            st.setTitle(bundle.getString("pedidos.window.title.detalles"));
            st.setScene(new Scene(root));
            st.initOwner(listaPedidos.getScene().getWindow());
            st.setResizable(false);
            st.centerOnScreen();
            st.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al abrir detalles de pedido", ex);
        }
    }

    @FXML
    private void regresarPrincipal(javafx.event.ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Rutas.VISTAS.PRINCIPAL), bundle);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1_000, 700));
            stage.setTitle(bundle.getString("window.title.principal"));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al volver a la vista principal", ex);
        }
    }
}
