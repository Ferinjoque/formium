package fabrica.personalizacion;

import modelo.diseno.ElementoDiseno;

/**
 * <strong>Abstract Factory</strong> para familias de {@link ElementoDiseno}.
 *
 * <p>Cada implementación concreta produce componentes coherentes entre sí
 * (texto, imagen&hairsp;…) que comparten un estilo o conjunto de
 * características.</p>
 */
public interface FactoriaAbstractaPersonalizacion {

    /**
     * Crea un elemento de texto personalizado.
     *
     * @param texto       contenido textual
     * @param fuente      nombre de la fuente tipográfica
     * @param color       color en notación CSS/hex
     * @param precioExtra coste adicional por la personalización
     * @return instancia de {@link ElementoDiseno} que representa el texto
     */
    ElementoDiseno crearElementoTexto(String texto,
                                      String fuente,
                                      String color,
                                      double precioExtra);

    /**
     * Crea un elemento de imagen personalizado.
     *
     * @param urlImagen   URL de la imagen
     * @param descripcion descripción alt/SEO
     * @param precioExtra coste adicional por la personalización
     * @return instancia de {@link ElementoDiseno} que representa la imagen
     */
    ElementoDiseno crearElementoImagen(String urlImagen,
                                       String descripcion,
                                       double precioExtra);
}
