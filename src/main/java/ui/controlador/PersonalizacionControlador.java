package ui.controlador;

import aplicacion.GestorDeEstado;
import fabrica.personalizacion.FactoriaAbstractaPersonalizacion;
import fabrica.personalizacion.FactoriaPersonalizacionBasica;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import modelo.carrito.CarritoCompras;
import modelo.catalogo.CatalogoProductos;
import modelo.diseno.ElementoDiseno;
import modelo.diseno.ElementoImagen;
import modelo.diseno.ElementoTexto;
import modelo.diseno.GrupoElementosDiseno;
import modelo.producto.*;
import recuerdo.DisenoOriginador;
import recuerdo.HistorialDiseno;
import recuerdo.RecuerdoDiseno;
import servicio.ServicioInventario;
import servicio.ServicioNotificacionesUI;
import util.Rutas;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Controlador de la vista de personalización de productos.
 * Gestiona el ciclo completo de edición de texto/imagen, variantes de producto,
 * historial (deshacer / rehacer) y sincronización de stock.
 */
public class PersonalizacionControlador implements Initializable {

    /* -------------------------- constantes -------------------------- */
    private static final Logger  LOGGER               = Logger.getLogger(PersonalizacionControlador.class.getName());
    private static final double  PRECIO_EXTRA_TEXTO   = 5.50;
    private static final double  PRECIO_EXTRA_IMAGEN  = 12.00;
    private static final int     MAX_LARGO_TEXTO      = 7;
    private static final double  MAX_PREVIEW          = 480;
    private static final double  ESCALA_IMG           = 0.80;

    /* ------------------------------ FXML ---------------------------- */
    @FXML private Label lblNombreProducto, lblDescripcionProducto,
            lblPrecioBase, lblExtraTexto, lblExtraImagen, lblPrecioTotal;
    @FXML private VBox  panelDeHerramientas, controlesTexto, controlesImagen;
    @FXML private ChoiceBox<String> tipoDeDisenoSelector;
    @FXML private TextField txtTextoPersonalizado;
    @FXML private ComboBox<String> txtFuenteTexto, selectorDeImagen, comboTalla, comboColor;
    @FXML private ColorPicker txtColorTexto;
    @FXML private Button btnAnadirOEditar, btnEliminar, btnDeshacer, btnRehacer;
    @FXML private StackPane lienzoProducto;
    @FXML private ImageView imagenProducto, imagenColorOverlay;
    @FXML private ScrollPane scrollHerr;

    /* ---------------------------- modelo ---------------------------- */
    private Producto                             productoEnPersonalizacion;
    private final FactoriaAbstractaPersonalizacion fabricaPersonalizacion = new FactoriaPersonalizacionBasica();
    private DisenoOriginador                     disenoOriginador;
    private final HistorialDiseno                historialDiseno = new HistorialDiseno();
    private Pane                                 contenedorDeZona;
    private GrupoElementosDiseno                 zonaDeDisenoUnica;
    private List<ProductoBase>                   variantesDisponibles;
    private boolean                              isUpdatingCombos;
    private ResourceBundle                       bundle;

    /* ---------------------------------------------------------------- */
    /*  Inicialización                                                  */
    /* ---------------------------------------------------------------- */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bundle = resourceBundle;

        txtFuenteTexto.setItems(FXCollections.observableArrayList(Font.getFamilies()));
        txtFuenteTexto.getSelectionModel().select("Arial");
        txtColorTexto.setValue(Color.BLACK);

        tipoDeDisenoSelector.setItems(FXCollections.observableArrayList("Texto", "Imagen"));
        tipoDeDisenoSelector.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> mostrarControles("Texto".equals(n)));

        txtTextoPersonalizado.setTextFormatter(new TextFormatter<>(c ->
                c.getControlNewText().length() <= MAX_LARGO_TEXTO ? c : null));

        selectorDeImagen.setItems(FXCollections.observableArrayList(listarImagenesDeRecursos()));

        btnDeshacer.disableProperty().bind(historialDiseno.hayDeshacerProperty().not());
        btnRehacer .disableProperty().bind(historialDiseno.hayRehacerProperty().not());
    }

    /* ---------------------------------------------------------------- */
    /*  Set-up de producto                                              */
    /* ---------------------------------------------------------------- */

    public void setProducto(Producto producto) {
        ServicioInventario svc = new ServicioInventario();
        variantesDisponibles = CatalogoProductos.obtenerInstancia()
                .encontrarVariantesPorNombre(producto.obtenerNombre());

        ProductoBase inicial = variantesDisponibles.stream()
                .filter(v -> svc.obtenerStockDeProducto(v) > 0)
                .findFirst()
                .orElse((ProductoBase) producto);

        disenoOriginador = new DisenoOriginador(inicial);
        historialDiseno.guardar(disenoOriginador.guardarEstado());
        actualizarVistaDesdeOriginador();
        ajustarSeleccionSiSinStock();
    }

    /* ---------------------------------------------------------------- */
    /*  Vista y variantes                                               */
    /* ---------------------------------------------------------------- */

    private void actualizarVistaDesdeOriginador() {
        Producto prod = disenoOriginador.getProducto();
        actualizarVistaParaProducto((ProductoBase) prod);

        zonaDeDisenoUnica = ((ProductoBase) prod).obtenerElementosDiseno().isEmpty()
                ? null
                : (GrupoElementosDiseno) ((ProductoBase) prod).obtenerElementosDiseno().get(0);

        actualizarPanelDeHerramientas();
        refrescarVistaPrevia();
        actualizarDesgloseDePrecio();
    }

    private void actualizarVistaParaProducto(ProductoBase producto) {
        isUpdatingCombos = true;
        this.productoEnPersonalizacion = producto.clonar();

        comboTalla.setVisible(false); comboTalla.getItems().clear();
        comboColor.setVisible(false); comboColor.getItems().clear();

        if (producto instanceof ProductoCamiseta cam) {
            configurarCombos(cam.obtenerTalla(), cam.obtenerColor(), ProductoCamiseta.class);
        } else if (producto instanceof ProductoChaqueta cha) {
            configurarCombos(cha.obtenerTalla(), cha.obtenerColor(), ProductoChaqueta.class);
        } else if (producto instanceof ProductoPantalones pan) {
            configurarCombos(pan.obtenerTalla(), pan.obtenerColor(), ProductoPantalones.class);
        }

        lblNombreProducto.setText(bundle.getString("personalizacion.label.nombre_producto")
                .formatted(producto.obtenerNombre()));
        lblDescripcionProducto.setText(producto.obtenerDescripcion());

        aplicarTinteDeColor(comboColor.getValue());
        actualizarDesgloseDePrecio();
        isUpdatingCombos = false;
    }

    private <T extends ProductoBase> void configurarCombos(String tallaSel,
                                                           String colorSel,
                                                           Class<T> type) {

        comboTalla.setVisible(true);
        comboColor.setVisible(true);

        comboTalla.setItems(FXCollections.observableArrayList(
                variantesDisponibles.stream()
                        .filter(type::isInstance)
                        .map(p -> obtenerAtributo(p, "talla"))
                        .distinct()
                        .sorted()
                        .toList()));

        poblarColoresParaTalla(tallaSel);

        comboTalla.setValue(tallaSel);

        if (colorSel != null && comboColor.getItems().contains(colorSel)) {
            comboColor.setValue(colorSel);
        } else {
            comboColor.getSelectionModel().clearSelection();
        }
    }

    /** Llena comboColor con los colores de la talla dada sin provocar IndexOutOfBounds */
    private void poblarColoresParaTalla(String talla) {

        List<String> nuevos = variantesDisponibles.stream()
                .filter(pb -> obtenerAtributo(pb, "talla").equals(talla))
                .map(pb -> obtenerAtributo(pb, "color"))
                .distinct()
                .sorted()
                .toList();

        String colorActual = comboColor.getValue();

        ObservableList<String> items = comboColor.getItems();
        if (items == null) {
            comboColor.setItems(FXCollections.observableArrayList(nuevos));
        } else {
            items.setAll(nuevos);
        }

        if (colorActual != null && nuevos.contains(colorActual)) {
            comboColor.setValue(colorActual);
        } else {
            comboColor.getSelectionModel().clearSelection();
        }
    }

    private void ajustarSeleccionSiSinStock() {
        ServicioInventario svc = new ServicioInventario();

        String tallaSel  = comboTalla.getValue();
        String colorSel  = comboColor.getValue();

        if (tallaSel != null && colorSel != null) {
            Optional<ProductoBase> actual = encontrarVariante(tallaSel, colorSel);
            if (actual.isPresent() && svc.obtenerStockDeProducto(actual.get()) > 0) {
                return;
            }
        }

        Optional<ProductoBase> conStock = variantesDisponibles.stream()
                .filter(pb -> svc.obtenerStockDeProducto(pb) > 0)
                .findFirst();

        if (conStock.isPresent()) {
            isUpdatingCombos = true;
            ProductoBase pb = conStock.get();

            comboTalla.setValue(obtenerAtributo(pb, "talla"));
            poblarColoresParaTalla(comboTalla.getValue());
            comboColor.setValue(obtenerAtributo(pb, "color"));
            isUpdatingCombos = false;

            disenoOriginador.setProducto(pb);
            actualizarDesgloseDePrecio();
        } else {
            panelDeHerramientas.setDisable(true);
            ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                    bundle.getString("personalizacion.notif.stock_agotado.title"),
                    bundle.getString("personalizacion.notif.stock_agotado.msg_total")
                            .formatted(productoEnPersonalizacion.obtenerNombre()),
                    lienzoProducto);
        }
    }

    /* ---------------------------------------------------------------- */
    /*  Precio                                                           */
    /* ---------------------------------------------------------------- */
    private void actualizarDesgloseDePrecio() {
        if (productoEnPersonalizacion == null) return;

        double precioBaseVariante = ((ProductoBase) productoEnPersonalizacion).getPrecioBase();

        double extraTexto  = 0;
        double extraImagen = 0;
        if (zonaDeDisenoUnica != null && !zonaDeDisenoUnica.getElementos().isEmpty()) {
            ElementoDiseno ed = zonaDeDisenoUnica.getElementos().get(0);
            if (ed instanceof ElementoTexto  et) extraTexto  = et.obtenerPrecioAdicional();
            if (ed instanceof ElementoImagen ei) extraImagen = ei.obtenerPrecioAdicional();
        }

        double total = precioBaseVariante + extraTexto + extraImagen;

        java.util.function.DoubleFunction<String> f =
                v -> String.format("S/ %,.2f", v);

        lblPrecioBase .setText(f.apply(precioBaseVariante));
        lblExtraTexto .setText("+ " + f.apply(extraTexto));
        lblExtraImagen.setText("+ " + f.apply(extraImagen));
        lblPrecioTotal.setText(f.apply(total));
    }

    /* ---------------------------------------------------------------- */
    /*  Panel de herramientas                                            */
    /* ---------------------------------------------------------------- */

    private void actualizarPanelDeHerramientas() {
        boolean hayDiseno = zonaDeDisenoUnica != null && !zonaDeDisenoUnica.getElementos().isEmpty();
        btnAnadirOEditar.setText(hayDiseno
                ? bundle.getString("personalizacion.btn.actualizar_diseno")
                : bundle.getString("personalizacion.btn.anadir_diseno"));
        btnEliminar.setVisible(hayDiseno);

        if (!hayDiseno) {
            tipoDeDisenoSelector.setValue("Texto");
            limpiarCampos();
            return;
        }

        ElementoDiseno el = zonaDeDisenoUnica.getElementos().get(0);
        if (el instanceof ElementoTexto et) {
            tipoDeDisenoSelector.setValue("Texto");
            txtTextoPersonalizado.setText(et.getTexto());
            txtFuenteTexto.setValue(et.getFuente());
            txtColorTexto.setValue(Color.web(et.getColor()));
        } else if (el instanceof ElementoImagen ei) {
            tipoDeDisenoSelector.setValue("Imagen");
            selectorDeImagen.setValue(ei.getUrlImagen()
                    .substring(ei.getUrlImagen().lastIndexOf('/') + 1));
        }
    }

    /* ---------------------------------------------------------------- */
    /*  Acciones de diseño                                               */
    /* ---------------------------------------------------------------- */

    @FXML
    void aplicarDiseno(ActionEvent e) {
        String tipo = tipoDeDisenoSelector.getValue();
        ElementoDiseno nuevo = crearElementoDesdeUI(tipo, (Node) e.getSource());
        if (nuevo == null) return;

        disenoOriginador.setElementoEnZona(zonaDeDisenoUnica.getNombreGrupo(), nuevo);
        historialDiseno.guardar(disenoOriginador.guardarEstado());

        actualizarVistaDesdeOriginador();
        Platform.runLater(() -> scrollHerr.setVvalue(1));
    }

    private ElementoDiseno crearElementoDesdeUI(String tipo, Node owner) {
        if ("Texto".equals(tipo)) {
            String txt = txtTextoPersonalizado.getText().trim();
            if (txt.isEmpty()) {
                notificar("personalizacion.notif.error.sin_contenido_texto", owner);
                return null;
            }
            return fabricaPersonalizacion.crearElementoTexto(
                    txt, txtFuenteTexto.getValue(), colorToHex(txtColorTexto.getValue()), PRECIO_EXTRA_TEXTO);
        }
        if ("Imagen".equals(tipo)) {
            String name = selectorDeImagen.getValue();
            if (name == null || name.isBlank()) {
                notificar("personalizacion.notif.error.sin_contenido_imagen", owner);
                return null;
            }
            return fabricaPersonalizacion.crearElementoImagen(
                    Rutas.IMAGENES.RUTA_DISENOS + name, name, PRECIO_EXTRA_IMAGEN);
        }
        return null;
    }

    @FXML
    void eliminarDiseno(ActionEvent e) {
        disenoOriginador.limpiarZona(zonaDeDisenoUnica.getNombreGrupo());
        historialDiseno.guardar(disenoOriginador.guardarEstado());
        refrescarVistaPrevia();
        actualizarPanelDeHerramientas();
        actualizarDesgloseDePrecio();
    }

    /* ---------------------------------------------------------------- */
    /*  Vista previa                                                     */
    /* ---------------------------------------------------------------- */

    public void refrescarLayoutInicial() {
        configurarVistaPrevia();
        refrescarVistaPrevia();
    }

    private void configurarVistaPrevia() {
        String rutaBase = (productoEnPersonalizacion instanceof ProductoChaqueta) ? Rutas.IMAGENES.CHAQUETA_BASE
                : (productoEnPersonalizacion instanceof ProductoPantalones) ? Rutas.IMAGENES.PANTALON_BASE
                : Rutas.IMAGENES.CAMISETA_BASE;

        Image base = new Image(getClass().getResourceAsStream(rutaBase));
        imagenProducto.setImage(base);
        imagenColorOverlay.setImage(base);

        imagenProducto.setPreserveRatio(true);
        imagenColorOverlay.setPreserveRatio(true);

        DoubleBinding lado = Bindings.createDoubleBinding(
                () -> Math.min(MAX_PREVIEW,
                        Math.min(lienzoProducto.getWidth(), lienzoProducto.getHeight()) - 20),
                lienzoProducto.widthProperty(), lienzoProducto.heightProperty());

        imagenProducto.fitWidthProperty().bind(lado);
        imagenProducto.fitHeightProperty().bind(lado);
        imagenColorOverlay.fitWidthProperty().bind(lado);
        imagenColorOverlay.fitHeightProperty().bind(lado);

        if (contenedorDeZona != null) lienzoProducto.getChildren().remove(contenedorDeZona);
        contenedorDeZona = new Pane();
        contenedorDeZona.setMouseTransparent(true);
        lienzoProducto.getChildren().add(contenedorDeZona);

        double offsetY = productoEnPersonalizacion.obtenerNombre().toLowerCase().contains("chaqueta") ? -60
                : productoEnPersonalizacion.obtenerNombre().toLowerCase().contains("pantalones") ? 20 : -40;
        StackPane.setMargin(contenedorDeZona, new Insets(offsetY, 0, 0, 0));

        refrescarVistaPrevia();
        aplicarTinteDeColor(comboColor.getValue());
    }

    private void refrescarVistaPrevia() {
        if (contenedorDeZona == null || zonaDeDisenoUnica == null || zonaDeDisenoUnica.getElementos().isEmpty()) {
            if (contenedorDeZona != null) contenedorDeZona.getChildren().clear();
            return;
        }

        contenedorDeZona.getChildren().clear();
        ElementoDiseno elemento = zonaDeDisenoUnica.getElementos().get(0);

        double cw = imagenProducto.getBoundsInLocal().getWidth();
        double ch = imagenProducto.getBoundsInLocal().getHeight();

        double w = cw * zonaDeDisenoUnica.getPrefWidth();
        double h = ch * zonaDeDisenoUnica.getPrefHeight();

        double left = cw * zonaDeDisenoUnica.getLayoutX() - w / 2;
        double top  = ch * zonaDeDisenoUnica.getLayoutY() - h / 2;

        Node nodo = (elemento instanceof ElementoTexto et) ? crearPreviewTexto(et, w, h)
                : (elemento instanceof ElementoImagen ei) ? crearPreviewImagen(ei, w, h)
                : null;

        if (nodo != null) {
            nodo.setLayoutX(left);
            nodo.setLayoutY(top);
            contenedorDeZona.getChildren().add(nodo);
        }
    }

    private Node crearPreviewTexto(ElementoTexto et, double w, double h) {
        Text txt = new Text(et.getTexto());
        txt.setFont(Font.font(et.getFuente(), Math.min(w, h) * 0.45));
        txt.setFill(Color.web(et.getColor()));
        txt.setWrappingWidth(w);
        txt.setTextAlignment(TextAlignment.CENTER);

        StackPane sp = new StackPane(txt);
        sp.setPrefSize(w, h);
        sp.setAlignment(Pos.CENTER);
        return sp;
    }

    private Node crearPreviewImagen(ElementoImagen ei, double w, double h) {
        ImageView iv = new ImageView();
        try {
            iv.setImage(new Image(getClass().getResourceAsStream(ei.getUrlImagen())));
        } catch (Exception ex) {
            iv.setImage(new Image(Rutas.IMAGENES.ERROR_PLACEHOLDER));
            LOGGER.log(Level.WARNING, "Imagen de diseño no encontrada: " + ei.getUrlImagen(), ex);
        }
        iv.setPreserveRatio(true);
        iv.setFitWidth(w * ESCALA_IMG);
        iv.setFitHeight(h * ESCALA_IMG);

        StackPane sp = new StackPane(iv);
        sp.setPrefSize(w, h);
        sp.setAlignment(Pos.CENTER);
        return sp;
    }

    /* ---------------------------------------------------------------- */
    /*  Deshacer / rehacer                                              */
    /* ---------------------------------------------------------------- */
    private void restaurarDesdeMemento(RecuerdoDiseno rec) {
        if (rec != null) {
            disenoOriginador.restaurarEstado(rec);
            actualizarVistaDesdeOriginador();
        }
    }
    @FXML private void deshacerUltimaAccion(ActionEvent e) { restaurarDesdeMemento(historialDiseno.deshacer()); }
    @FXML private void rehacerUltimaAccion(ActionEvent e)  { restaurarDesdeMemento(historialDiseno.rehacer ()); }

    /* ---------------------------------------------------------------- */
    /*  Confirmar personalización                                       */
    /* ---------------------------------------------------------------- */
    @FXML
    private void confirmarPersonalizacion(ActionEvent e) {
        if (hayCambiosSinGuardar()) {
            if (!confirmar("personalizacion.alert.cambios_sin_guardar"))
                return;
        }

        ServicioInventario svc = new ServicioInventario();
        CarritoCompras carrito = GestorDeEstado.obtenerInstancia().getCarrito();
        int stockDisp = svc.obtenerStockDisponible((ProductoBase) productoEnPersonalizacion, carrito);

        if (stockDisp <= 0) {
            notificar("personalizacion.notif.stock_agotado",
                    (Node) e.getSource(),
                    descripcionVariante((ProductoBase) productoEnPersonalizacion));
            return;
        }

        ProductoBase pb = (ProductoBase) productoEnPersonalizacion;
        pb.limpiarElementosDiseno();
        pb.anadirElementoDiseno(zonaDeDisenoUnica.clonar());

        carrito.anadirProducto(productoEnPersonalizacion);
        regresarPrincipal(e, true);
    }

    /* ---------------------------------------------------------------- */
    /*  Auxiliares                                                      */
    /* ---------------------------------------------------------------- */

    private boolean confirmar(String key) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                bundle.getString(key + ".content"),
                ButtonType.OK, ButtonType.CANCEL);
        a.setTitle(bundle.getString(key + ".title"));
        a.setHeaderText(bundle.getString(key + ".header"));
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    private boolean hayCambiosSinGuardar() {
        String tipo = tipoDeDisenoSelector.getValue();
        boolean vacia = zonaDeDisenoUnica == null || zonaDeDisenoUnica.getElementos().isEmpty();

        if (vacia) {
            return "Texto".equals(tipo) ? !txtTextoPersonalizado.getText().trim().isEmpty()
                    : "Imagen".equals(tipo) ? selectorDeImagen.getValue() != null
                    : false;
        }

        ElementoDiseno el = zonaDeDisenoUnica.getElementos().get(0);
        if (el instanceof ElementoTexto et && "Texto".equals(tipo)) {
            return !(et.getTexto().equals(txtTextoPersonalizado.getText().trim()) &&
                    et.getFuente().equals(txtFuenteTexto.getValue()) &&
                    et.getColor().equalsIgnoreCase(colorToHex(txtColorTexto.getValue())));
        }
        if (el instanceof ElementoImagen ei && "Imagen".equals(tipo)) {
            String sel = selectorDeImagen.getValue();
            return sel != null && !ei.getUrlImagen().endsWith('/' + sel);
        }
        return true;
    }

    private void mostrarControles(boolean texto) {
        controlesTexto .setVisible(texto); controlesTexto .setManaged(texto);
        controlesImagen.setVisible(!texto); controlesImagen.setManaged(!texto);
    }

    private String descripcionVariante(ProductoBase pb) {
        return "%s (%s - %s)".formatted(
                pb.obtenerNombre(),
                obtenerAtributo(pb, "talla"),
                obtenerAtributo(pb, "color"));
    }

    private List<String> listarImagenesDeRecursos() {
        try {
            URL u = getClass().getResource(Rutas.IMAGENES.RUTA_DISENOS);
            if (u == null) return List.of();
            Path dir = Paths.get(u.toURI());
            try (Stream<Path> st = Files.walk(dir, 1)) {
                return st.filter(p -> !Files.isDirectory(p))
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .filter(n -> n.matches(".*\\.(png|jpg|jpeg)"))
                        .sorted()
                        .toList();
            }
        } catch (URISyntaxException | IOException ex) {
            LOGGER.log(Level.SEVERE, "Error listando imágenes de diseño", ex);
            return List.of();
        }
    }

    private void limpiarCampos() {
        txtTextoPersonalizado.clear();
        selectorDeImagen.getSelectionModel().clearSelection();
        txtColorTexto.setValue(Color.BLACK);
        txtFuenteTexto.getSelectionModel().select("Arial");
    }

    private void regresarPrincipal(ActionEvent e, boolean notif) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Rutas.VISTAS.PRINCIPAL), bundle);
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1_000, 700));
            stage.centerOnScreen();
            stage.show();

            if (notif) {
                ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                        bundle.getString("personalizacion.notif.confirmacion.title"),
                        bundle.getString("personalizacion.notif.confirmacion.msg"),
                        root);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al volver a la vista principal", ex);
            notificar("alert.title.error_carga", (Node) e.getSource());
        }
    }

    @FXML
    private void regresarPrincipal(ActionEvent e) {
        regresarPrincipal(e, /*notif=*/ false);
    }

    private void notificar(String key, Node owner, Object... params) {
        String titulo = bundle.getString(key + ".title");
        String cuerpo = bundle.getString(key + ".msg");
        if (params.length > 0) {
            cuerpo = String.format(cuerpo, params);
        }
        ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(titulo, cuerpo, owner);
    }

    private void aplicarTinteDeColor(String colorName) {
        if (colorName == null || imagenColorOverlay == null) {
            imagenColorOverlay.setEffect(null);
            return;
        }
        try {
            Color c = Color.web(colorName.toLowerCase());
            if (Color.WHITE.equals(c)) { imagenColorOverlay.setEffect(null); return; }

            javafx.scene.effect.Lighting l = new javafx.scene.effect.Lighting(
                    new javafx.scene.effect.Light.Distant(45, 45, c));
            l.setDiffuseConstant(1.2);
            l.setSpecularConstant(0.2);
            l.setSurfaceScale(0);
            imagenColorOverlay.setEffect(l);
        } catch (Exception ex) {
            imagenColorOverlay.setEffect(null);
        }
    }

    private String colorToHex(Color c) {
        return "#%02X%02X%02X".formatted(
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }

    /* ---------------------------------------------------------------- */
    /*  Cambio de variante                                              */
    /* ---------------------------------------------------------------- */

    @FXML
    private void onVariantChanged() {
        if (isUpdatingCombos) return;

        try {
            isUpdatingCombos = true;

            String tallaSeleccionada = comboTalla.getValue();
            String colorSeleccionado = comboColor.getValue();

            if (tallaSeleccionada == null) return;

            poblarColoresParaTalla(tallaSeleccionada);
            colorSeleccionado = comboColor.getValue();

            if (colorSeleccionado == null) {
                return;
            }

            ServicioInventario svc = new ServicioInventario();
            Optional<ProductoBase> seleccion = encontrarVariante(tallaSeleccionada, colorSeleccionado);

            if (seleccion.isPresent() && svc.obtenerStockDeProducto(seleccion.get()) > 0) {
                disenoOriginador.setProducto(seleccion.get());
                actualizarVistaDesdeOriginador();
            } else {
                variantesDisponibles.stream()
                        .filter(pb -> svc.obtenerStockDeProducto(pb) > 0)
                        .findFirst()
                        .ifPresentOrElse(alternativa -> {
                            comboTalla.setValue(obtenerAtributo(alternativa, "talla"));
                            poblarColoresParaTalla(obtenerAtributo(alternativa, "talla"));
                            comboColor.setValue(obtenerAtributo(alternativa, "color"));
                            disenoOriginador.setProducto(alternativa);
                            actualizarVistaDesdeOriginador();
                        }, () -> {
                            panelDeHerramientas.setDisable(true);
                            ServicioNotificacionesUI.obtenerInstancia().mostrarNotificacion(
                                    bundle.getString("personalizacion.notif.stock_agotado.title"),
                                    bundle.getString("personalizacion.notif.stock_agotado.msg_total")
                                            .formatted(productoEnPersonalizacion.obtenerNombre()),
                                    lienzoProducto);
                        });
            }
        } finally {
            isUpdatingCombos = false;
        }
    }

    private Optional<ProductoBase> encontrarVariante(String talla, String color) {
        return variantesDisponibles.stream()
                .filter(pb -> obtenerAtributo(pb, "talla").equals(talla) &&
                        obtenerAtributo(pb, "color").equals(color))
                .findFirst();
    }

    private String obtenerAtributo(ProductoBase p, String attr) {
        return switch (attr) {
            case "talla" -> (p instanceof ProductoCamiseta c) ? c.obtenerTalla()
                    : (p instanceof ProductoChaqueta c) ? c.obtenerTalla()
                    : (p instanceof ProductoPantalones c) ? c.obtenerTalla() : "";
            case "color" -> (p instanceof ProductoCamiseta c) ? c.obtenerColor()
                    : (p instanceof ProductoChaqueta c) ? c.obtenerColor()
                    : (p instanceof ProductoPantalones c) ? c.obtenerColor() : "";
            default -> "";
        };
    }
}
