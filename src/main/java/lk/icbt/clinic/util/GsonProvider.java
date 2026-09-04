package lk.icbt.clinic.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * One shared, correctly configured {@link Gson} instance.
 * <p>
 * Gson has no built-in support for {@code java.time} types, so each one needs
 * an explicit adapter -- there is no Jackson-style auto-configuration doing
 * this behind the scenes.
 */
public final class GsonProvider {

    private static final Gson INSTANCE = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, type, ctx) ->
                            new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalTime.class,
                    (JsonSerializer<LocalTime>) (src, type, ctx) ->
                            new JsonPrimitive(src.format(DateTimeFormatter.ofPattern("HH:mm"))))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) ->
                            new JsonPrimitive(src.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, ctx) ->
                            LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
            .registerTypeAdapter(LocalTime.class,
                    (JsonDeserializer<LocalTime>) (json, type, ctx) ->
                            LocalTime.parse(json.getAsString().length() == 5
                                    ? json.getAsString() + ":00" : json.getAsString()))
            .create();

    private GsonProvider() {
    }

    public static Gson get() {
        return INSTANCE;
    }
}
