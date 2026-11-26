package frostyLib.util;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.Arrays;

public interface SavedConfig {
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) @interface Path {
        String value();
    }

    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .excludeFieldsWithModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL, Modifier.TRANSIENT)
        .registerTypeAdapter(InputConstants.Key.class, (JsonSerializer<InputConstants.Key>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getName()))
        .registerTypeAdapter(InputConstants.Key.class, (JsonDeserializer<InputConstants.Key>) (json, typeOfT, context) -> InputConstants.getKey(json.getAsString()))
        .create();

    class MissingPath extends IllegalStateException {
        public final Class<?> config;

        public MissingPath(Class<?> config) {
            super("No path was given and " + config.getName() + " has no @SavedConfig.Path annotation.");
            this.config = config;
        }
    }

    private static String path(Class<?> config) {
        if (config.getAnnotation(Path.class) == null) {
            throw new MissingPath(config);
        }
        return config.getAnnotation(Path.class).value();
    }

    static JsonObject serialize(Class<?> config) {
        try {
            JsonObject json = new JsonObject();
            for (Field field : config.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
                    field.setAccessible(true);
                    json.add(field.getName(), gson.toJsonTree(field.get(null), field.getGenericType()));
                }
            }
            return json;
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    default JsonObject serialize() {
        try {
            JsonObject json = new JsonObject();
            for (Field field : getClass().getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
                    field.setAccessible(true);
                    json.add(field.getName(), gson.toJsonTree(field.get(null), field.getGenericType()));
                }
            }
            return json;
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }

    private static void deserialize(Class<?> config, Object object, JsonElement json) {
        if (json instanceof JsonObject jsonObject) {
            for (Field field : config.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) == (object == null) && !Modifier.isFinal(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()) && jsonObject.has(field.getName())) {
                    try {
                        Object value = gson.fromJson(jsonObject.get(field.getName()), field.getGenericType());
                        if (value != null || Arrays.stream(field.getAnnotations()).anyMatch(annotation -> annotation.annotationType().getSimpleName().equals("Nullable"))) {
                            field.setAccessible(true);
                            field.set(object, value);
                        }
                    } catch (Throwable error) {}
                }
            }
        }
    }
    static void deserialize(Class<?> config, JsonElement json) {
        deserialize(config, null, json);
    }
    default void deserialize(JsonElement json) {
        deserialize(getClass(), this, json);
    }

    static void save(Class<?> config, java.nio.file.Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, gson.toJson(serialize(config)));
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    static void save(Class<?> config, String path) {
        save(config, FabricLoader.getInstance().getConfigDir().resolve(path));
    }
    static void save(Class<?> config) {
        save(config, path(config));
    }

    default <T extends SavedConfig> T save(java.nio.file.Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, gson.toJson(this));
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
        return (T) this;
    }
    default <T extends SavedConfig> T save(String path) {
        return save(FabricLoader.getInstance().getConfigDir().resolve(path));
    }
    default <T extends SavedConfig> T save() {
        return save(path(this.getClass()));
    }

    static void load(Class<?> config, java.nio.file.Path path) {
        if (Files.exists(path)) {
            try {
                deserialize(config, JsonParser.parseString(Files.readString(path)));
            } catch (Throwable error) {}
        }
    }
    static void load(Class<?> config, String path) {
        load(config, FabricLoader.getInstance().getConfigDir().resolve(path));
    }
    static void load(Class<?> config) {
        load(config, path(config));
    }

    default <T extends SavedConfig> T load(java.nio.file.Path path) {
        if (Files.exists(path)) {
            try {
                deserialize(JsonParser.parseString(Files.readString(path)));
            } catch (Throwable error) {}
        }
        return (T) this;
    }
    default <T extends SavedConfig> T load(String path) {
        return load(FabricLoader.getInstance().getConfigDir().resolve(path));
    }
    default <T extends SavedConfig> T load() {
        return load(path(this.getClass()));
    }

    static void autoSave(Class<?> config, java.nio.file.Path path) {
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> save(config, path));
    }
    static void autoSave(Class<?> config, String path) {
        autoSave(config, FabricLoader.getInstance().getConfigDir().resolve(path));
    }
    static void autoSave(Class<?> config) {
        autoSave(config, path(config));
    }

    default <T extends SavedConfig> T autoSave(java.nio.file.Path path) {
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> this.save(path));
        return (T) this;
    }
    default <T extends SavedConfig> T autoSave(String path) {
        return autoSave(FabricLoader.getInstance().getConfigDir().resolve(path));
    }
    default <T extends SavedConfig> T autoSave() {
        return autoSave(path(this.getClass()));
    }

    default <T extends SavedConfig> T copy() {
        return (T) gson.fromJson(gson.toJsonTree(this), getClass());
    }
}
