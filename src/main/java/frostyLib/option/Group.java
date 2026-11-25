package frostyLib.option;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.BooleanSupplier;

public class Group extends ListWidget.Option {
    @Override public Group visible(BooleanSupplier visible) {
        super.visible(visible);
        return this;
    }
    @Override public Group visible(boolean visible) {
        super.visible(visible);
        return this;
    }

    private boolean indentChildren = false;
    public Group indentChildren(boolean indentChildren) {
        this.indentChildren = indentChildren;
        return this;
    }
    public boolean indentChildren() {
        return indentChildren;
    }

    private final List<ListWidget.Option> children = new ArrayList<>();

    public void add(ListWidget.Option... options) {
        children.addAll(Arrays.asList(options));
    }
    public void add(int index, ListWidget.Option... options) {
        children.addAll(index, Arrays.asList(options));
    }

    public void remove(int index) {
        children.remove(index);
    }
    public void remove(ListWidget.Option... options) {
        children.removeAll(Arrays.asList(options));
    }

    public void clear() {
        children.clear();
    }

    public ListWidget.Option get(int index) {
        return children.get(index);
    }

    public int indexOf(ListWidget.Option option) {
        return children.indexOf(option);
    }

    public boolean contains(ListWidget.Option... options) {
        return new HashSet<>(children).containsAll(Arrays.asList(options));
    }

    public int size() {
        return children.size();
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    @Override public List<AbstractWidget> widgets() {
        List<AbstractWidget> widgets = new ArrayList<>();
        for (ListWidget.Option child : children) {
            widgets.addAll(child.widgets());
        }
        return widgets;
    }

    @Override public int height(int width) {
        int height = 0;
        for (ListWidget.Option option : children) {
            height += option.visible() ? option.height(width - (indentChildren ? 26 : 0)) : 0;
        }
        return height;
    }

    @Override public int render(GuiGraphicsExtractor graphics, int x, int y, int width/*? if <26.4-* {*//*, int mouseX, int mouseY, float deltaTicks*//*?}*/) {
        int height = 0;
        for (ListWidget.Option child : children) {
            if (child.visible()) {
                height += child.render(graphics, x + (indentChildren ? 26 : 0), y + height, width - (indentChildren ? 26 : 0)/*? if <26.4-* {*//*, mouseX, mouseY, deltaTicks*//*?}*/);
            }
        }
        return height;
    }
}
