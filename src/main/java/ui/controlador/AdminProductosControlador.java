package ui.controlador;

import fabrica.FactoriaProducto;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import modelo.admin.StockProducto;
import modelo.admin.request.CrearCamisetaRequest;
import modelo.admin.request.CrearChaquetaRequest;
import modelo.admin.request.CrearPantalonesRequest;
import modelo.catalogo.CatalogoProductos;
import modelo.inventario.Inventario;
import modelo.producto.*;
import servicio.ServicioInventario;
import servicio.ServicioNotificacionesUI;
import util.AppLogic;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controlador de la pestaña “Productos” del panel de administración.
 *
 * <p>Permite crear, editar y eliminar productos de los tipos definidos en
 * {@link AppLogic.TIPOS_PRODUCTO}, así como mantener la tabla de inventario
 * sincronizada.</p>
 */
public class AdminProductosControlador implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AdminProductosControlador.class.getName());

    /* ---------------------------------- FXML ---------------------------------- */
    @FXML private ListView<Producto>    listaProductosAdmin;
    @FXML private TextField             txtNombre;
    @FXML private TextArea              txtDescripcion;
    @FXML private TextField             txtPrecioBase;
    @FXML private ComboBox<String>      comboTipoProducto;
    @FXML private GridPane              gridParametrosEspecificos;
    @FXML private Label                 lblParam1, lblParam2, lblParam3;
    @FXML private ComboBox<String>      comboParam1, comboParam2, comboParam3;
    @FXML private Button                btnGuardar;

    /* -------------------------------- lógica ---------------------------------- */
    private final ObservableList<Producto> productosObservables = FXCollections.observableArrayList();
    private AdminInventarioControlador     adminInventarioControlador;
    private ChangeListener<Object>         formChangeListener;
    private boolean                        enModoCreacion = false;
    private ResourceBundle                 bundle;

    /* catálogos auxiliares para combos */
    private final ObservableList<String> tallasRopa         = FXCollections.observableArrayList("S","M","L","XL");
    private final ObservableList<String> tallasPantalones   = FXCollections.observableArrayList("30","32","34","36");
    private final ObservableList<String> colores            = FXCollections.observableArrayList("White","Black","Red","Blue","Gray","Green","Navy","Khaki","Olive","Beige");
    private final ObservableList<String> materialesChaqueta = FXCollections.observableArrayList("Jean","Cuero","Drill");
    private final ObservableList<String> cortesPantalones   = FXCollections.observableArrayList("Slim Fit","Regular","Cargo","Ancho");

    /* ------------------------------------------------------------------------- */
    /*  Ciclo de vida                                                            */
    /* ------------------------------------------------------------------------- */

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bundle = resourceBundle;

        productosObservables.addAll(CatalogoProductos.obtenerInstancia().obtenerTodosLosProductos());
        listaProductosAdmin.setItems(productosObservables);

        listaProductosAdmin.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Producto p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : StockProducto.obtenerNombreDetallado(p));
            }
        });

        comboTipoProducto.setItems(FXCollections.observableArrayList(
                AppLogic.TIPOS_PRODUCTO.CAMISETA,
                AppLogic.TIPOS_PRODUCTO.CHAQUETA,
                AppLogic.TIPOS_PRODUCTO.PANTALONES));

        formChangeListener = (o, ov, nv) -> actualizarEstadoBotonGuardar();

        listaProductosAdmin.getSelectionModel().selectedItemProperty()
                .addListener((o, oldSel, newSel) -> {
                    enModoCreacion = (newSel == null);
                    if (newSel != null) cargarDatosProductoEnFormulario(newSel);
                });

        comboTipoProducto.valueProperty().addListener((o, ov, nv) -> {
            actualizarCamposEspecificos(nv);
            actualizarEstadoBotonGuardar();
        });

        handleNuevo(null); // inicia en modo creación
    }

    /* ---------------------- inyección de controlador inventario ---------------------- */

    public void setAdminInventarioControlador(AdminInventarioControlador ctrl) {
        adminInventarioControlador = ctrl;
    }

    /* ------------------------------------------------------------------------- */
    /*  Configuración de campos específicos por tipo                             */
    /* ------------------------------------------------------------------------- */

    private void actualizarCamposEspecificos(String tipo) {
        boolean visible = tipo != null;
        gridParametrosEspecificos.setVisible(visible);
        gridParametrosEspecificos.setManaged(visible);

        lblParam1.setVisible(false); comboParam1.setVisible(false);
        lblParam2.setVisible(false); comboParam2.setVisible(false);
        lblParam3.setVisible(false); comboParam3.setVisible(false);

        if (tipo == null) return;

        switch (tipo) {
            case AppLogic.TIPOS_PRODUCTO.CAMISETA -> {
                configurarCampo(lblParam1, comboParam1, "admin.productos.label.talla", tallasRopa);
                configurarCampo(lblParam2, comboParam2, "admin.productos.label.color", colores);
            }
            case AppLogic.TIPOS_PRODUCTO.CHAQUETA -> {
                configurarCampo(lblParam1, comboParam1, "admin.productos.label.talla", tallasRopa);
                configurarCampo(lblParam2, comboParam2, "admin.productos.label.material", materialesChaqueta);
                configurarCampo(lblParam3, comboParam3, "admin.productos.label.color", colores);
            }
            case AppLogic.TIPOS_PRODUCTO.PANTALONES -> {
                configurarCampo(lblParam1, comboParam1, "admin.productos.label.talla", tallasPantalones);
                configurarCampo(lblParam2, comboParam2, "admin.productos.label.corte", cortesPantalones);
                configurarCampo(lblParam3, comboParam3, "admin.productos.label.color", colores);
            }
        }
    }

    private void configurarCampo(Label lbl, ComboBox<String> combo, String key, ObservableList<String> items) {
        lbl.setText(bundle.getString(key));
        combo.setItems(items);
        lbl.setVisible(true);
        combo.setVisible(true);
    }

    /* ------------------------------------------------------------------------- */
    /*  Carga de un producto existente en el formulario                          */
    /* ------------------------------------------------------------------------- */

    private void cargarDatosProductoEnFormulario(Producto p) {
        quitarListeners();
        limpiarFormulario(false);

        txtNombre.setText(p.obtenerNombre());
        txtDescripcion.setText(p.obtenerDescripcion());
        txtPrecioBase.setText(String.valueOf(((ProductoBase) p).getPrecioBase()));

        txtNombre.setDisable(true);
        comboTipoProducto.setDisable(true);
        comboParam1.setDisable(true); comboParam2.setDisable(true); comboParam3.setDisable(true);

        if (p instanceof ProductoCamiseta pc) {
            comboTipoProducto.setValue(AppLogic.TIPOS_PRODUCTO.CAMISETA);
            comboParam1.setValue(pc.obtenerTalla());
            comboParam2.setValue(pc.obtenerColor());
        } else if (p instanceof ProductoChaqueta pc) {
            comboTipoProducto.setValue(AppLogic.TIPOS_PRODUCTO.CHAQUETA);
            comboParam1.setValue(pc.obtenerTalla());
            comboParam2.setValue(pc.obtenerMaterial());
            comboParam3.setValue(pc.obtenerColor());
        } else if (p instanceof ProductoPantalones pp) {
            comboTipoProducto.setValue(AppLogic.TIPOS_PRODUCTO.PANTALONES);
            comboParam1.setValue(pp.obtenerTalla());
            comboParam2.setValue(pp.obtenerCorte());
            comboParam3.setValue(pp.obtenerColor());
        }

        anadirListeners();
        actualizarEstadoBotonGuardar();
    }

    /* ------------------------------------------------------------------------- */
    /*  Botón Guardar                                                            */
    /* ------------------------------------------------------------------------- */

    private void actualizarEstadoBotonGuardar() {
        if (enModoCreacion) {
            boolean ok = !txtNombre.getText().isBlank()
                    && !txtDescripcion.getText().isBlank()
                    && !txtPrecioBase.getText().isBlank()
                    && comboTipoProducto.getValue() != null;
            btnGuardar.setDisable(!ok);
        } else {
            Producto sel = listaProductosAdmin.getSelectionModel().getSelectedItem();
            if (sel == null) { btnGuardar.setDisable(true); return; }

            boolean descCamb   = !txtDescripcion.getText().equals(sel.obtenerDescripcion());
            boolean precioCamb = false;
            try {
                precioCamb = Double.parseDouble(txtPrecioBase.getText()) != ((ProductoBase) sel).getPrecioBase();
            } catch (NumberFormatException ignore) { }
            btnGuardar.setDisable(!descCamb && !precioCamb);
        }
    }

    @FXML
    void handleGuardar(ActionEvent e) {
        String nombre = txtNombre.getText();
        String desc   = txtDescripcion.getText();
        String tipo   = comboTipoProducto.getValue();

        if (nombre.isBlank() || desc.isBlank() || tipo == null || txtPrecioBase.getText().isBlank()) {
            notificarError(bundle.getString("admin.productos.notif.error_campos_obligatorios.msg"), e);
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(txtPrecioBase.getText());
        } catch (NumberFormatException ex) {
            notificarError(bundle.getString("admin.productos.notif.error_precio_invalido.msg"), e);
            return;
        }

        if (precio <= 0) {
            notificarError("El precio base debe ser un número positivo.", e);
            return;
        }

        Producto productoGuardado = enModoCreacion
                ? crearNuevoProducto(tipo, nombre, desc, precio, e)
                : actualizarProductoExistente(precio, desc);

        if (productoGuardado == null) return;

        refrescarListaYSeleccionar(productoGuardado);
        Optional.ofNullable(adminInventarioControlador).ifPresent(AdminInventarioControlador::refrescarTablaInventario);

        ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                bundle.getString("admin.productos.notif.exito_guardado.title"),
                bundle.getString("admin.productos.notif.exito_guardado.msg"),
                (Node) e.getSource());
    }

    /* ---------------------- creación / actualización ---------------------- */

    private Producto crearNuevoProducto(String tipo, String nombre, String desc,
                                        double precio, ActionEvent e) {

        Producto productoGuardado = switch (tipo) {
            case AppLogic.TIPOS_PRODUCTO.CAMISETA -> {
                if (faltanParams(comboParam1, comboParam2)) { notificarParams("Talla y Color", e); yield null; }
                yield CatalogoProductos.obtenerInstancia().guardarProducto(
                        FactoriaProducto.crearProducto(new CrearCamisetaRequest(
                                nombre, desc, precio, comboParam1.getValue(), comboParam2.getValue())));
            }
            case AppLogic.TIPOS_PRODUCTO.CHAQUETA -> {
                if (faltanParams(comboParam1, comboParam2, comboParam3)) { notificarParams("Talla, Material y Color", e); yield null; }
                yield CatalogoProductos.obtenerInstancia().guardarProducto(
                        FactoriaProducto.crearProducto(new CrearChaquetaRequest(
                                nombre, desc, precio, comboParam1.getValue(), comboParam2.getValue(), comboParam3.getValue())));
            }
            case AppLogic.TIPOS_PRODUCTO.PANTALONES -> {
                if (faltanParams(comboParam1, comboParam2, comboParam3)) { notificarParams("Talla, Corte y Color", e); yield null; }
                yield CatalogoProductos.obtenerInstancia().guardarProducto(
                        FactoriaProducto.crearProducto(new CrearPantalonesRequest(
                                nombre, desc, precio, comboParam1.getValue(), comboParam2.getValue(), comboParam3.getValue())));
            }
            default -> null;
        };

        if (productoGuardado != null) {
            new ServicioInventario().crearRegistroInventario(
                    new Inventario((ProductoBase) productoGuardado, 1));
        }
        return productoGuardado;
    }

    private Producto actualizarProductoExistente(double precio, String desc) {
        Producto sel = listaProductosAdmin.getSelectionModel().getSelectedItem();
        if (sel == null) return null;
        ((ProductoBase) sel).setDescripcion(desc);
        ((ProductoBase) sel).setPrecioBase(precio);
        return CatalogoProductos.obtenerInstancia().guardarProducto(sel);
    }

    /* ------------------------------------------------------------------------- */
    /*  Eliminar                                                                  */
    /* ------------------------------------------------------------------------- */

    @FXML
    void handleEliminar(ActionEvent e) {
        Producto sel = listaProductosAdmin.getSelectionModel().getSelectedItem();
        if (sel == null) {
            notificarError(bundle.getString("admin.productos.notif.error_seleccionar_eliminar.msg"), e);
            return;
        }

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                bundle.getString("admin.productos.alert.confirmar_eliminacion.msg")
                        .formatted(StockProducto.obtenerNombreDetallado(sel)),
                ButtonType.YES, ButtonType.NO);

        conf.showAndWait().filter(r -> r == ButtonType.YES).ifPresent(r -> {
            CatalogoProductos.obtenerInstancia().eliminarProducto(sel);
            refrescarListaYSeleccionar(null);
            Optional.ofNullable(adminInventarioControlador).ifPresent(AdminInventarioControlador::refrescarTablaInventario);

            ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                    bundle.getString("admin.productos.notif.exito_eliminado.title"),
                    bundle.getString("admin.productos.notif.exito_eliminado.msg"),
                    (Node) e.getSource());

            handleNuevo(null);
        });
    }

    /* ------------------------------------------------------------------------- */
    /*  Nuevo                                                                    */
    /* ------------------------------------------------------------------------- */

    @FXML
    void handleNuevo(ActionEvent e) {
        enModoCreacion = true;
        listaProductosAdmin.getSelectionModel().clearSelection();
        quitarListeners();
        limpiarFormulario(true);

        txtNombre.setDisable(false);
        txtDescripcion.setDisable(false);
        txtPrecioBase.setDisable(false);
        comboTipoProducto.setDisable(false);
        comboParam1.setDisable(false); comboParam2.setDisable(false); comboParam3.setDisable(false);

        anadirListeners();
        actualizarEstadoBotonGuardar();
        txtNombre.requestFocus();
    }

    /* ------------------------------------------------------------------------- */
    /*  Helpers                                                                  */
    /* ------------------------------------------------------------------------- */

    private void refrescarListaYSeleccionar(Producto sel) {
        Long idSel = (sel instanceof ProductoBase pb) ? pb.getId() : null;
        productosObservables.setAll(CatalogoProductos.obtenerInstancia().obtenerTodosLosProductos());
        if (idSel != null) {
            productosObservables.stream()
                    .filter(p -> ((ProductoBase) p).getId().equals(idSel))
                    .findFirst()
                    .ifPresent(p -> {
                        listaProductosAdmin.getSelectionModel().select(p);
                        listaProductosAdmin.scrollTo(p);
                    });
        }
    }

    private void quitarListeners() {
        txtDescripcion.textProperty().removeListener(formChangeListener);
        txtPrecioBase.textProperty().removeListener(formChangeListener);
    }

    private void anadirListeners() {
        txtDescripcion.textProperty().addListener(formChangeListener);
        txtPrecioBase.textProperty().addListener(formChangeListener);
    }

    private void limpiarFormulario(boolean limpiarTipo) {
        txtNombre.clear(); txtDescripcion.clear(); txtPrecioBase.clear();
        comboParam1.getSelectionModel().clearSelection();
        comboParam2.getSelectionModel().clearSelection();
        comboParam3.getSelectionModel().clearSelection();
        if (limpiarTipo) {
            comboTipoProducto.getSelectionModel().clearSelection();
            actualizarCamposEspecificos(null);
        }
    }

    private void notificarError(String msg, ActionEvent e) {
        ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                bundle.getString("admin.productos.notif.error_validacion.title"),
                msg,
                (Node) e.getSource());
    }

    private void notificarParams(String params, ActionEvent e) {
        notificarError(bundle.getString("admin.productos.notif.error_params_obligatorios.msg").formatted(params), e);
    }

    private boolean faltanParams(ComboBox<?>... combos) {
        for (ComboBox<?> c : combos) {
            if (c.getValue() == null) return true;
        }
        return false;
    }
}
