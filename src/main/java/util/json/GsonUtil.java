package util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import modelo.diseno.ElementoDiseno;
import modelo.diseno.ElementoImagen;
import modelo.diseno.ElementoTexto;
import modelo.diseno.GrupoElementosDiseno;

/**
 * Utilidad estática que provee una única instancia de {@link Gson}
 * configurada para (de-)serializar correctamente la jerarquía de
 * {@link ElementoDiseno} usando un campo <code>"type"</code>.
 *
 * <p>La instancia se crea de forma «lazy» y es *thread-safe*
 * gracias al bloqueo implícito sobre el método {@link #getGson()}.</p>
 */
public final class GsonUtil {

    /** Instancia singleton de Gson configurada para los elementos de diseño. */
    private static Gson gson;

    /** Bloquea la construcción; clase de utilidades. */
    private GsonUtil() { }

    /**
     * Devuelve la instancia de {@link Gson} compartida.
     * Si aún no existe, la construye registrando el {@link RuntimeTypeAdapterFactory}
     * con las subclases concretas.
     */
    public static synchronized Gson getGson() {
        if (gson == null) {
            RuntimeTypeAdapterFactory<ElementoDiseno> rtaFactory =
                    RuntimeTypeAdapterFactory.of(ElementoDiseno.class, "type")
                            .registerSubtype(GrupoElementosDiseno.class, "grupo")
                            .registerSubtype(ElementoTexto.class,  "texto")
                            .registerSubtype(ElementoImagen.class, "imagen");

            gson = new GsonBuilder()
                    .registerTypeAdapterFactory(rtaFactory)
                    .create();
        }
        return gson;
    }
}
