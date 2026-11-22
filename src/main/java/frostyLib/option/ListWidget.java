package frostyLib.option;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

class ListWidget extends AbstractContainerWidget {
    private static final Identifier menuListBackground = Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final Identifier inworldMenuListBackground = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");

    public final List<Option> options;
    private int padding = 3;
    private int contentHeight = 4;

    public ListWidget(List<Option> options) {
        super(0, 0, 0, 0, CommonComponents.EMPTY, AbstractScrollArea.defaultSettings(10));
        this.options = options;
    }

    public ListWidget() {
        this(new ArrayList<>());
    }

    public List<AbstractWidget> widgets() {
        List<AbstractWidget> widgets = new ArrayList<>();
        for (Option option : options) {
            if (option.visible()) {
                widgets.addAll(option.widgets());
            }
        }
        return widgets;
    }

    @Override
    public @NonNull List<? extends GuiEventListener> children() {
        return widgets();
    }

    public void setPadding(int padding) {
        this.padding = Math.max(3, padding);
    }

    public int getPadding() {
        return padding;
    }

    @Override
    public double scrollAmount() {
        setScrollAmount(super.scrollAmount());
        return super.scrollAmount();
    }

    @Override
    protected int scrollBarX() {
        return this.width - Math.max(scrollbarWidth(), padding) + 3;
    }

    @Override
    protected int contentHeight() {
        return contentHeight;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, Minecraft.getInstance().level == null ? menuListBackground : inworldMenuListBackground, getX(), getY(), getRight(), getBottom() + (int) scrollAmount(), getWidth(), getHeight(), 32, 32);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Minecraft.getInstance().level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR, getX(), getY() - 2, 0, 0, getWidth(), 2, 32, 2);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Minecraft.getInstance().level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR, getX(), getBottom(), 0, 0, getWidth(), 2, 32, 2);
        graphics.enableScissor(getX(), getY(), getRight(), getBottom());
        int height = 4;
        boolean overflows = false;
        for (Option option : options) {
            if (option.visible()) {
                height += option.height(getWidth() - 2 * padding);
                if (height > getHeight()) {
                    overflows = true;
                    break;
                }
            }
        }
        height = 4;
        int y = getY() - (int) scrollAmount() - 2;
        for (Option option : options) {
            if (option.visible()) {
                height += option.render(graphics, padding, y + height, getWidth() - padding - Math.max(overflows ? scrollbarWidth() : 0, padding), mouseX, mouseY, deltaTicks);
            }
        }
        contentHeight = height;
        graphics.disableScissor();
        extractScrollbar(graphics, mouseX, mouseY);
    }

    @Override
    public void updateWidgetNarration(@NonNull NarrationElementOutput output) {}

    public static class Option {
        private BooleanSupplier visible = () -> true;

        public Option visible(BooleanSupplier visible) {
            this.visible = visible;
            return this;
        }

        public Option visible(boolean visible) {
            return visible(() -> visible);
        }

        public boolean visible() {
            return this.visible.getAsBoolean();
        }

        public List<AbstractWidget> widgets() {
            return List.of();
        }

        public int height(int width) {
            return 0;
        }

        public int render(GuiGraphicsExtractor graphics, int x, int y, int width, int mouseX, int mouseY, float deltaTicks) {
            for (AbstractWidget widget : widgets()) {
                widget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
            }
            return 0;
        }
    }
}
