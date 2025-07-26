package ui.controlador;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import modelo.admin.StockProducto;
import modelo.diseno.ElementoDiseno;
import modelo.diseno.ElementoImagen;
import modelo.diseno.ElementoTexto;
import modelo.diseno.GrupoElementosDiseno;
import modelo.pedido.ItemPedido;
import modelo.pedido.Pedido;
import modelo.producto.ProductoBase;
import modelo.producto.ProductoCamiseta;
import modelo.producto.ProductoChaqueta;
import modelo.producto.ProductoPantalones;
import util.Rutas;
import util.json.GsonUtil;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Ventana que muestra los detalles y la previsualización de un {@link Pedido}.
 */
public class DetallesPedidoControlador {

    private static final Logger LOGGER       = Logger.getLogger(DetallesPedidoControlador.class.getName());

    private static final double PREVIEW_SIZE = 180.0;
    private static final double ESCALA_IMG   = 0.80;
    private static final String ERROR_IMG    = "https://via.placeholder.com/100?text=Error";

    /* ----------------------------- FXML ----------------------------- */
    @FXML private Label lblIdPedido, lblEstado, lblFecha, lblTotal;
    @FXML private VBox  contenedorItems;

    /* ---------------------------- modelo ---------------------------- */
    private Pedido pedido;
    private final ResourceBundle bundle = ResourceBundle.getBundle("ui.vista.messages");

    /* ---------------------------------------------------------------- */
    /*  API pública                                                     */
    /* ---------------------------------------------------------------- */

    public void setPedido(Pedido p) {
        pedido = p;
        mostrarDetalles();
    }

    /* ---------------------------------------------------------------- */
    /*  Renderizado                                                     */
    /* ---------------------------------------------------------------- */

    private void mostrarDetalles() {
        if (pedido == null) return;

        lblIdPedido.setText(bundle.getString("detalles_pedido.label.id").formatted(pedido.obtenerIdPedido()));
        lblEstado  .setText(pedido.obtenerEstado());
        lblFecha   .setText(pedido.obtenerFechaCreacion()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lblTotal   .setText("S/%.2f".formatted(pedido.obtenerCostoTotal()));

        contenedorItems.getChildren().clear();
        Gson gson = GsonUtil.getGson();
        pedido.getItems().forEach(it -> contenedorItems.getChildren().add(crearVistaItem(it, gson)));
    }

    private VBox crearVistaItem(ItemPedido item, Gson gson) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.TOP_CENTER);

        Label nombre = new Label(StockProducto.obtenerNombreDetallado(item.getProductoBase()));
        nombre.getStyleClass().add("sidebar-title");

        StackPane lienzo = new StackPane();
        lienzo.setAlignment(Pos.CENTER);
        lienzo.setPrefSize(PREVIEW_SIZE, PREVIEW_SIZE);

        Image baseImg = cargarImagenBase(item.getProductoBase());
        ImageView base   = crearImagen(baseImg);
        ImageView tinted = crearImagen(baseImg);
        aplicarTinte(item, tinted);

        lienzo.getChildren().addAll(base, tinted);

        String json = item.getPersonalizacionJson();
        if (json != null && !json.isEmpty()) {
            Type t = new TypeToken<List<GrupoElementosDiseno>>() {}.getType();
            List<GrupoElementosDiseno> zonas = gson.fromJson(json, t);
            zonas.stream().findFirst()
                    .flatMap(z -> z.getElementos().stream().findFirst().map(el -> z))
                    .map(this::crearNodoDeDiseno)
                    .ifPresent(lienzo.getChildren()::add);
        }

        card.getChildren().addAll(nombre, lienzo);
        return card;
    }

    /* ---------------------------------------------------------------- */
    /*  Helpers                                                         */
    /* ---------------------------------------------------------------- */

    private Image cargarImagenBase(ProductoBase p) {
        String ruta = switch (p) {
            case ProductoChaqueta __    -> Rutas.IMAGENES.CHAQUETA_BASE;
            case ProductoPantalones __  -> Rutas.IMAGENES.PANTALON_BASE;
            default                     -> Rutas.IMAGENES.CAMISETA_BASE;
        };
        return new Image(getClass().getResourceAsStream(ruta));
    }

    private ImageView crearImagen(Image img) {
        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setFitWidth(PREVIEW_SIZE);
        iv.setFitHeight(PREVIEW_SIZE);
        return iv;
    }

    private Node crearNodoDeDiseno(GrupoElementosDiseno zona) {
        ElementoDiseno elemento = zona.getElementos().get(0);
        double zonaW = PREVIEW_SIZE * zona.getPrefWidth();
        double zonaH = PREVIEW_SIZE * zona.getPrefHeight();

        Node nodoVisual = (elemento instanceof ElementoTexto et) ? crearTexto(et, zonaW, zonaH, zona)
                : (elemento instanceof ElementoImagen ei) ? crearImagen(ei, zonaW, zonaH, zona)
                : null;

        if (nodoVisual != null) {
            double offsetX = (zona.getLayoutX() - 0.5) * PREVIEW_SIZE + zona.getAjusteX();
            double offsetY = (zona.getLayoutY() - 0.5) * PREVIEW_SIZE + zona.getAjusteY();
            nodoVisual.setTranslateX(offsetX);
            nodoVisual.setTranslateY(offsetY);
        }
        return nodoVisual;
    }

    private Node crearTexto(ElementoTexto et, double zonaW, double zonaH, GrupoElementosDiseno zona) {
        Text txt = new Text(et.getTexto());
        double size = zonaW * 0.20 * zona.getEscalaContenido();
        txt.setFont(Font.font(et.getFuente(), size));
        txt.setFill(Color.web(et.getColor()));
        txt.setWrappingWidth(zonaW);
        txt.setTextAlignment(TextAlignment.CENTER);

        StackPane sp = new StackPane(txt);
        sp.setPrefSize(zonaW, zonaH);
        sp.setAlignment(Pos.CENTER);
        return sp;
    }

    private Node crearImagen(ElementoImagen ei, double zonaW, double zonaH, GrupoElementosDiseno zona) {
        ImageView iv = new ImageView();
        try {
            iv.setImage(new Image(getClass().getResourceAsStream(ei.getUrlImagen())));
        } catch (Exception ex) {
            LOGGER.warning(() -> "Error cargando imagen de diseño: " + ei.getUrlImagen());
            iv.setImage(new Image(ERROR_IMG));
        }
        iv.setPreserveRatio(true);
        iv.setFitWidth(zonaW * ESCALA_IMG * zona.getEscalaContenido());
        iv.setFitHeight(zonaH * ESCALA_IMG * zona.getEscalaContenido());

        StackPane sp = new StackPane(iv);
        sp.setPrefSize(zonaW, zonaH);
        sp.setAlignment(Pos.CENTER);
        return sp;
    }

    private void aplicarTinte(ItemPedido item, ImageView iv) {
        String color = switch (item.getProductoBase()) {
            case ProductoCamiseta p    -> p.obtenerColor();
            case ProductoChaqueta p    -> p.obtenerColor();
            case ProductoPantalones p  -> p.obtenerColor();
            default                    -> null;
        };

        if (color == null) { iv.setEffect(null); return; }

        try {
            Color c = Color.web(color.toLowerCase());
            if (Color.WHITE.equals(c)) { iv.setEffect(null); return; }

            Lighting l = new Lighting();
            l.setLight(new Light.Distant(45, 45, c));
            l.setDiffuseConstant(1.2);
            l.setSpecularConstant(0.2);
            l.setSurfaceScale(0);
            iv.setEffect(l);
        } catch (Exception ex) {
            iv.setEffect(null);
        }
    }
}
