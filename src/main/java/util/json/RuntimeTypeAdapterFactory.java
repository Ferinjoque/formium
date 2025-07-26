package util.json;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Adaptador genérico para manejar polimorfismo en Gson. Permite serializar y
 * deserializar jerarquías de clases indicando el subtipo mediante un campo
 * (p. ej. <code>{"type":"texto", ...}</code>).</p>
 *
 * <p>La implementación es una versión ligeramente adaptada del patrón
 * publicado por Jesse Wilson / Google y liberado bajo la licencia Apache 2.0.</p>
 *
 * @param <T> super-tipo raíz de la jerarquía
 */
public final class RuntimeTypeAdapterFactory<T> implements TypeAdapterFactory {

    private final Class<?> baseType;
    private final String   typeFieldName;
    private final boolean  maintainType;
    private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<>();
    private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap<>();

    /* ----------------------- Fábricas de instancia ---------------------- */

    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType, String field) {
        return new RuntimeTypeAdapterFactory<>(baseType, field, false);
    }

    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType) {
        return of(baseType, "type");
    }

    private RuntimeTypeAdapterFactory(Class<?> baseType, String typeFieldName, boolean maintainType) {
        if (baseType == null || typeFieldName == null) throw new NullPointerException();
        this.baseType      = baseType;
        this.typeFieldName = typeFieldName;
        this.maintainType  = maintainType;
    }

    /* ------------------------- Registro de subtipos -------------------- */

    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> subtype, String label) {
        if (subtype == null || label == null) throw new NullPointerException();
        if (subtypeToLabel.containsKey(subtype) || labelToSubtype.containsKey(label)) {
            throw new IllegalArgumentException("Types and labels must be unique.");
        }
        labelToSubtype.put(label, subtype);
        subtypeToLabel.put(subtype, label);               // etiqueta coherente
        return this;
    }

    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> subtype) {
        return registerSubtype(subtype, subtype.getSimpleName());
    }

    /* ----------------------- Implementación de fábrica ----------------- */

    @Override
    public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        if (type.getRawType() != baseType) return null;

        TypeAdapter<JsonElement> jsonAdapter = gson.getAdapter(JsonElement.class);
        Map<String, TypeAdapter<?>> labelToDelegate = new LinkedHashMap<>();
        Map<Class<?>, TypeAdapter<?>> subtypeToDelegate = new LinkedHashMap<>();

        labelToSubtype.forEach((label, sub) -> {
            TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(sub));
            labelToDelegate.put(label, delegate);
            subtypeToDelegate.put(sub, delegate);
        });

        return new TypeAdapter<R>() {
            @Override public R read(JsonReader in) throws IOException {
                JsonElement json = jsonAdapter.read(in);
                JsonObject  obj  = json.getAsJsonObject();
                JsonElement labelEl = maintainType ? obj.get(typeFieldName) : obj.remove(typeFieldName);
                if (labelEl == null) {
                    throw new JsonParseException("Missing type field '" + typeFieldName + '\'');
                }
                String label = labelEl.getAsString();
                @SuppressWarnings("unchecked")
                TypeAdapter<R> delegate = (TypeAdapter<R>) labelToDelegate.get(label);
                if (delegate == null) {
                    throw new JsonParseException("Unknown subtype label '" + label + '\'');
                }
                return delegate.fromJsonTree(obj);
            }

            @Override public void write(JsonWriter out, R value) throws IOException {
                Class<?> srcType = value.getClass();
                @SuppressWarnings("unchecked")
                TypeAdapter<R> delegate = (TypeAdapter<R>) subtypeToDelegate.get(srcType);
                if (delegate == null) {
                    throw new JsonParseException("Unregistered subtype: " + srcType.getName());
                }

                JsonObject jsonObj = delegate.toJsonTree(value).getAsJsonObject();
                if (!maintainType) {
                    if (jsonObj.has(typeFieldName)) {
                        throw new JsonParseException(
                                "The field '" + typeFieldName + "' already exists in " + srcType.getName());
                    }
                    jsonObj = cloneWithType(jsonObj, subtypeToLabel.get(srcType));
                }
                Streams.write(jsonObj, out);
            }

            private JsonObject cloneWithType(JsonObject src, String label) {
                JsonObject clone = new JsonObject();
                clone.add(typeFieldName, new JsonPrimitive(label));
                src.entrySet().forEach(e -> clone.add(e.getKey(), e.getValue()));
                return clone;
            }
        }.nullSafe();
    }
}
