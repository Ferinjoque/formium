package recuerdo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Cuida la pila de <em>mementos</em> para operaciones de deshacer/rehacer
 * sobre un diseño.
 */
public class HistorialDiseno {

    private final Deque<RecuerdoDiseno> pilaRecuerdos = new ArrayDeque<>();
    private final Deque<RecuerdoDiseno> pilaRehacer   = new ArrayDeque<>();

    private final BooleanProperty hayDeshacer = new SimpleBooleanProperty(false);
    private final BooleanProperty hayRehacer  = new SimpleBooleanProperty(false);

    /* ----------------------------- operaciones ----------------------------- */

    public void guardar(RecuerdoDiseno recuerdo) {
        pilaRecuerdos.push(recuerdo);
        pilaRehacer.clear();
        actualizarEstados();
    }

    public RecuerdoDiseno deshacer() {
        if (pilaRecuerdos.size() > 1) {
            pilaRehacer.push(pilaRecuerdos.pop());
            actualizarEstados();
            return pilaRecuerdos.peek();
        }
        return null;
    }

    public RecuerdoDiseno rehacer() {
        if (!pilaRehacer.isEmpty()) {
            pilaRecuerdos.push(pilaRehacer.pop());
            actualizarEstados();
            return pilaRecuerdos.peek();
        }
        return null;
    }

    /* --------------------------- propiedades FX ---------------------------- */

    public BooleanProperty hayDeshacerProperty() { return hayDeshacer; }
    public BooleanProperty hayRehacerProperty()  { return hayRehacer; }

    /* ------------------------------ helpers -------------------------------- */

    private void actualizarEstados() {
        hayDeshacer.set(pilaRecuerdos.size() > 1);
        hayRehacer.set(!pilaRehacer.isEmpty());
    }
}
