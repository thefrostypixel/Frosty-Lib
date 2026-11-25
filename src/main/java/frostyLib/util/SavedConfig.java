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

    static void deserialize(Class<?> config, JsonObject json) {
        try {
            for (Field field : config.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()) && json.has(field.getName())) {
                    field.setAccessible(true);
                    field.set(null, gson.fromJson(json.get(field.getName()), field.getGenericType()));
                }
            }
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    default void deserialize(JsonObject json) {
        try {
            for (Field field : getClass().getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()) && json.has(field.getName())) {
                    field.setAccessible(true);
                    field.set(null, gson.fromJson(json.get(field.getName()), field.getGenericType()));
                }
            }
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
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
        try {
            if (Files.exists(path)) {
                JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                for (Field field : config.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()) && json.has(field.getName())) {
                        field.setAccessible(true);
                        field.set(null, gson.fromJson(json.get(field.getName()), field.getGenericType()));
                    }
                }
            }
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }
    static void load(Class<?> config, String path) {
        load(config, FabricLoader.getInstance().getConfigDir().resolve(path));
    }
    static void load(Class<?> config) {
        load(config, path(config));
    }

    default <T extends SavedConfig> T load(java.nio.file.Path path) {
        try {
            if (Files.exists(path)) {
                Object loaded = gson.fromJson(Files.readString(path), getClass());
                if (loaded != null) {
                    for (Field field : getClass().getDeclaredFields()) {
                        if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
                            field.setAccessible(true);
                            field.set(this, field.get(loaded));
                        }
                    }
                }
            }
        } catch (Throwable error) {
            throw new RuntimeException(error);
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
