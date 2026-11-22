package frostyLib.widget;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class ButtonWidget extends Button.Plain {
    private Component label = null;
    private Runnable onPress = null;

    public ButtonWidget() {
        super(0, 0, 0, 0, Component.empty(), button -> {}, textSupplier -> Component.empty());
    }

    public ButtonWidget label(Component label) {
        this.setMessage((this.label = label) == null ? Component.empty() : label);
        return this;
    }

    public ButtonWidget translatableLabel(String labelKey, Object... args) {
        this.setMessage((this.label = Component.translatable(labelKey, args)) == null ? Component.empty() : label);
        return this;
    }

    public ButtonWidget literalLabel(String labelText) {
        this.setMessage((this.label = Component.literal(labelText)) == null ? Component.empty() : label);
        return this;
    }

    public Component label() {
        return label;
    }

    public ButtonWidget onPress(Runnable onPress) {
        this.onPress = onPress;
        return this;
    }

    public Runnable onPress() {
        return onPress;
    }

    @Override
    public void onPress(@NonNull InputWithModifiers input) {
        if (onPress != null) {
            onPress.run();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() == 0 || click.button() == 1) {
            if (onPress != null) {
                onPress.run();
            }
            return true;
        }
        return false;
    }
}
