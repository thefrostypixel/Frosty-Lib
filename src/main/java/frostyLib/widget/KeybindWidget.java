package frostyLib.widget;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class KeybindWidget extends Button.Plain {
    private InputConstants.Key keybind = InputConstants.UNKNOWN;
    private Consumer<InputConstants.Key> onSelect = null;
    private boolean listening = false;

    public KeybindWidget() {
        super(0, 0, 0, 0, InputConstants.UNKNOWN.getDisplayName(), button -> {}, textSupplier -> Component.empty().append(InputConstants.UNKNOWN.getDisplayName()));
    }

    public KeybindWidget keybind(InputConstants.Key keybind) {
        this.keybind = keybind;
        this.listening = false;
        setMessage(keybind.getDisplayName());
        return this;
    }

    public InputConstants.Key keybind() {
        return keybind;
    }

    public KeybindWidget onSelect(Consumer<InputConstants.Key> onSelect) {
        this.onSelect = onSelect;
        return this;
    }

    public Consumer<InputConstants.Key> onSelect() {
        return onSelect;
    }

    @Override
    public void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        if (!this.isFocused()) {
            listening = false;
            setMessage(keybind.getDisplayName());
        }
        super.extractContents(graphics, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void onPress(@NonNull InputWithModifiers input) {
        super.onPress(input);
        super.setFocused(true);
        listening = true;
        setMessage(Component.literal("> ").append(Component.empty().append(keybind.getDisplayName()).withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE)).append(" <").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        if (listening) {
            listening = false;
            keybind = event.isEscape() ? InputConstants.UNKNOWN : InputConstants.getKey(event);
            setMessage(keybind.getDisplayName());
            if (onSelect != null) {
                onSelect.accept(keybind);
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (listening) {
            listening = false;
            keybind = InputConstants.Type.MOUSE.getOrCreate(event.button());
            setMessage(keybind.getDisplayName());
            if (onSelect != null) {
                onSelect.accept(keybind);
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
