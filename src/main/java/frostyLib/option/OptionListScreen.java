package frostyLib.option;

import frostyLib.widget.ButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionListScreen extends Screen {
    private final Screen parent;
    public final List<ListWidget.Option> options = new ArrayList<>();
    private final ListWidget listWidget = new ListWidget(options);
    public final List<ButtonWidget> buttons = new ArrayList<>(List.of(new ButtonWidget().label(CommonComponents.GUI_DONE).onPress(this::onClose)));

    public OptionListScreen(Screen parent, Component title) {
        super(title);
        this.parent = parent;
    }
    public OptionListScreen(Screen parent, String titleKey) {
        super(Component.translatable(titleKey));
        this.parent = parent;
    }

    public OptionListScreen buttons(ButtonWidget... buttons) {
        return this.buttons(Arrays.asList(buttons));
    }
    public OptionListScreen buttons(List<ButtonWidget> buttons) {
        this.buttons.clear();
        this.buttons.addAll(buttons);
        return this;
    }

    public OptionListScreen leadingButton(ButtonWidget button) {
        return this.leadingButtons(button);
    }
    public OptionListScreen leadingButtons(ButtonWidget... buttons) {
        return this.leadingButtons(Arrays.asList(buttons));
    }
    public OptionListScreen leadingButtons(List<ButtonWidget> buttons) {
        this.buttons.addAll(0, buttons);
        return this;
    }

    public OptionListScreen trailingButton(ButtonWidget button) {
        return this.trailingButtons(button);
    }
    public OptionListScreen trailingButtons(ButtonWidget... buttons) {
        return trailingButtons(Arrays.asList(buttons));
    }
    public OptionListScreen trailingButtons(List<ButtonWidget> buttons) {
        this.buttons.addAll(buttons);
        return this;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        clearWidgets();
        graphics.centeredText(font, getTitle(), width / 2, 10, CommonColors.WHITE);
        listWidget.setPosition(0, 30);
        listWidget.setSize(width, height - 64);
        listWidget.setPadding(32);
        listWidget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
        addWidget(listWidget);
        for (AbstractWidget child : listWidget.widgets()) {
            addWidget(child);
        }
        int buttonWidth = Math.min(200, (width - 40 - (buttons.size() - 1) * 6) / buttons.size());
        int x = (width - buttons.size() * buttonWidth - (buttons.size() - 1) * 6) / 2;
        for (ButtonWidget button : buttons) {
            button.setPosition(x, height - 26);
            button.setWidth(Math.min(width - 40, 200));
            button.setHeight(20);
            button.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
            addWidget(button);
            x += buttonWidth + 6;
        }
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        return this.getFocused() != null && this.getFocused().keyPressed(event) || super.keyPressed(event);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreenAndShow(parent);
    }
}
