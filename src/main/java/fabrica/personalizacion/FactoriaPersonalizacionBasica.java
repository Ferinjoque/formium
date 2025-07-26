package fabrica.personalizacion;

import modelo.diseno.ElementoDiseno;
import modelo.diseno.ElementoImagen;
import modelo.diseno.ElementoTexto;

/**
 * Fábrica concreta que genera elementos de diseño “básicos”
 * (sin estilos avanzados ni efectos especiales).
 */
public final class FactoriaPersonalizacionBasica
        implements FactoriaAbstractaPersonalizacion {

    @Override
    public ElementoDiseno crearElementoTexto(String texto,
                                             String fuente,
                                             String color,
                                             double precioExtra) {
        return new ElementoTexto(texto, fuente, color, precioExtra);
    }

    @Override
    public ElementoDiseno crearElementoImagen(String urlImagen,
                                              String descripcion,
                                              double precioExtra) {
        return new ElementoImagen(urlImagen, descripcion, precioExtra);
    }
}
