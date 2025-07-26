package observador;

/**
 * Parte «Publisher» del patrón <em>Observer</em>.
 */
public interface Sujeto {
    void anadirObservador(Observador observador);
}
