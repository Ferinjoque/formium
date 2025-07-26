package modelo.diseno;

import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Componente compuesto que agrupa varios {@link ElementoDiseno}.
 */
public class GrupoElementosDiseno implements ElementoDiseno {

    /* ----------------------------- metadatos layout ----------------------------- */
    private final String nombreGrupo;

    private final double layoutX;      // posición X relativa (0.0-1.0)
    private final double layoutY;      // posición Y relativa (0.0-1.0)
    private final double prefWidth;    // ancho relativo
    private final double prefHeight;   // alto relativo
    private final double ajusteX;      // desplazamiento X
    private final double ajusteY;      // desplazamiento Y
    private final double escalaContenido;

    private Pos alineacion = Pos.CENTER;

    /* -------------------------------- elementos -------------------------------- */
    private final List<ElementoDiseno> elementos = new ArrayList<>();

    /* --------------------------------- ctor(s) --------------------------------- */
    public GrupoElementosDiseno(String nombre,
                                double x, double y,
                                double w, double h,
                                double ajusteX, double ajusteY) {
        this(nombre, x, y, w, h, ajusteX, ajusteY, 1.0);
    }

    public GrupoElementosDiseno(String nombre,
                                double x, double y,
                                double w, double h) {
        this(nombre, x, y, w, h, 0, 0, 1.0);
    }

    public GrupoElementosDiseno(String nombre,
                                double x, double y,
                                double w, double h,
                                double ajusteX, double ajusteY,
                                double escala) {
        this.nombreGrupo      = nombre;
        this.layoutX          = x;
        this.layoutY          = y;
        this.prefWidth        = w;
        this.prefHeight       = h;
        this.ajusteX          = ajusteX;
        this.ajusteY          = ajusteY;
        this.escalaContenido  = escala;
    }

    /* ----------------------------- getters layout ------------------------------ */
    public String getNombreGrupo() { return nombreGrupo; }
    public double getLayoutX()     { return layoutX; }
    public double getLayoutY()     { return layoutY; }
    public double getPrefWidth()   { return prefWidth; }
    public double getPrefHeight()  { return prefHeight; }
    public Pos    getAlineacion()  { return alineacion; }
    public double getAjusteX()     { return ajusteX; }
    public double getAjusteY()     { return ajusteY; }
    public double getEscalaContenido() { return escalaContenido; }

    /* --------------------------------- API ------------------------------------- */

    /** Añade un componente (hoja o compuesto) al grupo. */
    public void anadirElemento(ElementoDiseno elemento) {
        if (elemento != null) {
            elementos.add(elemento);
        }
    }

    /** Elimina todos los elementos del grupo. */
    public void limpiarElementos() {
        elementos.clear();
    }

    /** @return vista de solo lectura de los elementos del grupo. */
    public List<ElementoDiseno> getElementos() {
        return Collections.unmodifiableList(elementos);
    }

    /* ----------------------------- Composite logic ----------------------------- */

    @Override
    public double obtenerPrecioAdicional() {
        return elementos.stream()
                .mapToDouble(ElementoDiseno::obtenerPrecioAdicional)
                .sum();
    }

    @Override
    public ElementoDiseno clonar() {
        GrupoElementosDiseno clon = new GrupoElementosDiseno(
                nombreGrupo, layoutX, layoutY,
                prefWidth, prefHeight,
                ajusteX, ajusteY,
                escalaContenido);

        clon.alineacion = this.alineacion;
        elementos.forEach(e -> clon.anadirElemento(e.clonar()));
        return clon;
    }
}
