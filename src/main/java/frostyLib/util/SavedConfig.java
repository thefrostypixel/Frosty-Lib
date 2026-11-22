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
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Path {
        String value();
    }

    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(InputConstants.Key.class, (JsonSerializer<InputConstants.Key>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getName()))
        .registerTypeAdapter(InputConstants.Key.class, (JsonDeserializer<InputConstants.Key>) (json, typeOfT, context) -> InputConstants.getKey(json.getAsString()))
        .create();

    default <T extends SavedConfig> T load() {
        Path annotation = getClass().getAnnotation(Path.class);
        if (annotation == null) {
            throw new IllegalStateException(getClass().getName() + " is missing @SavedConfig.Path");
        }
        java.nio.file.Path path = FabricLoader.getInstance().getConfigDir().resolve(annotation.value());
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

    default void save() {
        java.nio.file.Path path = FabricLoader.getInstance().getConfigDir().resolve(getClass().getAnnotation(Path.class).value());
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, gson.toJson(this));
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }

    default <T extends SavedConfig> T autoSave() {
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> this.save());
        return (T) this;
    }

    default <T extends SavedConfig> T copy() {
        return (T) gson.fromJson(gson.toJsonTree(this), getClass());
    }
}
