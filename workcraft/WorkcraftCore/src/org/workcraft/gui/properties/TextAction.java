package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

import java.awt.*;

public class TextAction {

    private final String text;
    private Action leftAction;
    private Action rightAction;
    private Color foreground;
    private Color background;

    public TextAction(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public TextAction setLeftAction(Action value) {
        leftAction = value;
        return this;
    }

    public Action getLeftAction() {
        return leftAction;
    }

    public TextAction setRightAction(Action value) {
        rightAction = value;
        return this;
    }

    public Action getRightAction() {
        return rightAction;
    }

    public TextAction setForeground(Color value) {
        foreground = value;
        return this;
    }

    public Color getForeground() {
        return foreground;
    }

    public TextAction setBackground(Color value) {
        background = value;
        return this;
    }

    public Color getBackground() {
        return background;
    }

}
