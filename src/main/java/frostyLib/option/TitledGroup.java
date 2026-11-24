package frostyLib.option;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class TitledGroup extends Group {
    private Component title = null;
    private Component description = null;
    private final Button button;
    private boolean collapsable = true;
    private boolean collapsed = false;

    public TitledGroup() {
        super.indentChildren(true);
        button = Button.builder(Component.literal("▼"), button -> collapsed(!collapsed)).build();
    }

    @Override
    public TitledGroup visible(BooleanSupplier visible) {
        super.visible(visible);
        return this;
    }

    @Override
    public TitledGroup visible(boolean visible) {
        super.visible(visible);
        return this;
    }

    @Override
    public TitledGroup indentChildren(boolean indentChildren) {
        super.indentChildren(indentChildren);
        return this;
    }

    public TitledGroup collapsable(boolean collapsable) {
        this.collapsable = collapsable;
        collapsed(collapsed);
        return this;
    }

    public boolean collapsable() {
        return collapsable;
    }

    public TitledGroup collapsed(boolean collapsed) {
        this.collapsed = collapsable && collapsed;
        button.setMessage(Component.literal(collapsed ? "▶" : "▼"));
        return this;
    }

    public boolean collapsed() {
        return collapsed;
    }

    public TitledGroup title(Component title) {
        this.title = title;
        return this;
    }

    public TitledGroup translatableTitle(String titleKey, Object... args) {
        this.title = Component.translatable(titleKey, args);
        return this;
    }

    public TitledGroup literalTitle(String titleText) {
        this.title = Component.literal(titleText);
        return this;
    }

    public Component title() {
        return title;
    }

    public TitledGroup description(Component description) {
        this.description = description;
        return this;
    }

    public TitledGroup translatableDescription(String descriptionKey, Object... args) {
        this.description = Component.translatable(descriptionKey, args);
        return this;
    }

    public TitledGroup literalDescription(String descriptionText) {
        this.description = Component.literal(descriptionText);
        return this;
    }

    public Component description() {
        return description;
    }

    @Override
    public List<AbstractWidget> widgets() {
        List<AbstractWidget> widgets = new ArrayList<>();
        if (collapsable) {
            widgets.add(button);
        }
        if (!collapsed) {
            widgets.addAll(super.widgets());
        }
        return widgets;
    }

    @Override
    public int height(int width) {
        return title() != null ? Math.max(3 + 9 * Minecraft.getInstance().font.split(title(), width - (collapsable ? 26 : 0)).size(), 20) : collapsable ? 20 : 0;
    }

    @Override
    public int render(GuiGraphicsExtractor graphics, int x, int y, int width/*? if <26.4-* {*//*, int mouseX, int mouseY, float deltaTicks*//*?}*/) {
        int height = collapsable ? 20 : 0;
        if (title() != null) {
            List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(title(), width - (collapsable ? 26 : 0));
            height = Math.max(3 + 9 * lines.size(), 20);
            double textY = y + (height - 9 * lines.size()) / 2. - 8.5;
            for (FormattedCharSequence line : lines) {
                graphics.text(Minecraft.getInstance().font, line, x + (collapsable ? 26 : 0), (int) Math.ceil(textY += 9), CommonColors.WHITE);
            }
        }
        if (collapsable) {
            button.setWidth(20);
            button.setHeight(20);
            button.setX(x);
            button.setY(y + height / 2 - 10);
            button.extractRenderState(graphics/*? if <26.4-* {*//*, mouseX, mouseY, deltaTicks*//*?}*/);
        }
        if (!collapsed) {
            return height + super.render(graphics, x, y + height, width/*? if <26.4-* {*//*, mouseX, mouseY, deltaTicks*//*?}*/);
        }
        return height;
    }
}
