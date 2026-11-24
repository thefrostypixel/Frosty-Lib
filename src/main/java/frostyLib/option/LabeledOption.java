package frostyLib.option;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

public class LabeledOption extends ListWidget.Option {
    private Component label = null;
    private Component description = null;
    private final List<AbstractWidget> widgets = new ArrayList<>();
    private final List<Double> widthWeights = new ArrayList<>();

    @Override
    public LabeledOption visible(BooleanSupplier visible) {
        super.visible(visible);
        return this;
    }

    @Override
    public LabeledOption visible(boolean visible) {
        super.visible(visible);
        return this;
    }

    public LabeledOption label(Component label) {
        this.label = label;
        return this;
    }

    public LabeledOption translatableLabel(String labelKey, Object... args) {
        this.label = Component.translatable(labelKey, args);
        return this;
    }

    public LabeledOption literalLabel(String labelText) {
        this.label = Component.literal(labelText);
        return this;
    }

    public Component label() {
        return label;
    }

    public LabeledOption description(Component description) {
        this.description = description;
        return this;
    }

    public LabeledOption translatableDescription(String descriptionKey, Object... args) {
        this.description = Component.translatable(descriptionKey, args);
        return this;
    }

    public LabeledOption literalDescription(String descriptionText) {
        this.description = Component.literal(descriptionText);
        return this;
    }

    public Component description() {
        return description;
    }

    public LabeledOption add(AbstractWidget widget) {
        return add(widget, 1);
    }

    public LabeledOption add(AbstractWidget widget, double widthWeight) {
        widgets.add(widget);
        widthWeights.add(widthWeight);
        return this;
    }

    @Override
    public List<AbstractWidget> widgets() {
        return widgets;
    }

    @Override
    public int height(int width) {
        return Math.max(3 + 9 * Minecraft.getInstance().font.split(label(), widgets.isEmpty() ? width : Math.max(width - 200, width / 2)).size(), 20);
    }

    @Override
    public int render(GuiGraphicsExtractor graphics, int x, int y, int width/*? if <26.4-* {*//*, int mouseX, int mouseY, float deltaTicks*//*?}*/) {
        int labelWidth = widgets.isEmpty() ? width : Math.max(width - 200, width / 2);
        List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(label(), labelWidth);
        int height = Math.max(3 + 9 * lines.size(), 20);
        double textY = y + (height - 9 * lines.size()) / 2. - 8.5;
        for (FormattedCharSequence line : lines) {
            graphics.text(Minecraft.getInstance().font, line, x, (int) Math.ceil(textY += 9), CommonColors.WHITE);
        }
        /*? if >=26.4-* {*/
        int mouseX = (int) Minecraft.getInstance().mouseHandler.getScaledXPos(Minecraft.getInstance().getWindow());
        int mouseY = (int) Minecraft.getInstance().mouseHandler.getScaledYPos(Minecraft.getInstance().getWindow());
        /*?}*/
        if (description != null && x <= mouseX && mouseX <= x + width && y <= mouseY && mouseY <= y + height) {
            graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, Arrays.stream(description.getString().split("\n")).map(s -> (Component) Component.literal(s)).toList(), mouseX, mouseY);
        }
        if (!widgets.isEmpty()) {
            int widgetsWidth = width - labelWidth;
            double totalWidthWeights = 0;
            for (int i = 0; i < widgets.size(); i++) {
                totalWidthWeights += widthWeights.get(i);
            }
            int right = x + width;
            x = right - widgetsWidth;
            for (int i = 0; i < widgets.size(); i++) {
                if (widgets.get(i) != null) {
                    widgets.get(i).setWidth(i + 1 == widgets.size() ? right - x : (int) (widgetsWidth * widthWeights.get(i) / totalWidthWeights));
                    widgets.get(i).setHeight(20);
                    widgets.get(i).setX(x);
                    widgets.get(i).setY(y + (height - widgets.get(i).getHeight()) / 2);
                    widgets.get(i).extractRenderState(graphics/*? if <26.4-* {*//*, mouseX, mouseY, deltaTicks*//*?}*/);
                    x += widgets.get(i).getWidth();
                }
            }
        }
        return height;
    }
}
