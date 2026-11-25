package frostyLib.widget;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

public class CycleButtonWidget<T extends CycleButtonWidget.Cyclable> extends Button.Plain {
    public static <T extends Enum<?> & Cyclable> CycleButtonWidget<T> ofSelected(T selected) {
        return new CycleButtonWidget<T>().options((T[]) selected.getClass().getEnumConstants()).selected(selected);
    }

    public CycleButtonWidget() {
        super(0, 0, 0, 0, Component.empty(), button -> {}, textSupplier -> Component.empty());
    }

    private T selected = null;
    public CycleButtonWidget<T> selected(T selected) {
        this.selected = selected;
        setMessage(selected == null ? Component.empty() : selected.text());
        return this;
    }
    public T selected() {
        return selected;
    }

    private T[] options = null;
    public CycleButtonWidget<T> options(T[] options) {
        this.options = options;
        return this;
    }
    public T[] options() {
        return options.clone();
    }

    private Consumer<T> onCycle = null;
    public CycleButtonWidget<T> onCycle(Consumer<T> onCycle) {
        this.onCycle = onCycle;
        return this;
    }
    public Consumer<T> onCycle() {
        return onCycle;
    }

    public void cycle(boolean forward) {
        if (options != null) {
            int index = -1;
            for (int i = 0; i < options.length; i++) {
                if (options[i] == selected) {
                    index = i;
                    break;
                }
            }
            if (index < 0 && options.length > 0) {
                selected = options[0];
            } else if (index > -1 && options.length > 0) {
                selected = options[(index + (forward ? 1 : -1) + options.length) % options.length];
            }
            this.setMessage(selected == null ? Component.empty() : selected.text());
            onCycle.accept(selected);
        }
    }

    @Override public void onPress(@NonNull InputWithModifiers input) {
        cycle(true);
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT || event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            cycle(event.button() == InputConstants.MOUSE_BUTTON_LEFT);
            return true;
        }
        return false;
    }

    public interface Cyclable {
        @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) @interface TranslationPath {
            String value();
        }

        default MutableComponent text() {
            TranslationPath path = getClass().getAnnotation(TranslationPath.class);
            String name = (this instanceof Enum<?> e) ? e.name() : toString();
            name = name.isEmpty() ? "" : Character.toLowerCase(name.charAt(0)) + name.substring(1);
            return path != null ? Component.translatable(path.value() + "." + name) : Component.literal(name);
        }
    }

    public enum Togglable implements Cyclable {
        On(true, Component.translatable("options.on")), Off(false, Component.translatable("options.off"));

        Togglable(boolean bool, MutableComponent text) {
            this.bool = bool;
            this.text = text;
        }

        public final boolean bool;

        private final MutableComponent text;
        @Override public MutableComponent text() {
            return text;
        }
    }
}
