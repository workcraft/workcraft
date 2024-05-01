package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

import java.awt.*;

public class TextAction {

    private final String text;
    private Action leftAction;
    private boolean modelModifyingLeftAction;
    private Action rightAction;
    private boolean modelModifyingRightAction;
    private Color foreground;
    private Color background;

    public TextAction(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public TextAction setLeftAction(Action value) {
        return setLeftAction(value, true);
    }
    public TextAction setLeftAction(Action value, boolean modifyModel) {
        leftAction = value;
        modelModifyingLeftAction = modifyModel;
        return this;
    }

    public Action getLeftAction() {
        return leftAction;
    }

    public boolean isModelModifyingLeftAction() {
        return modelModifyingLeftAction;
    }

    public TextAction setRightAction(Action value) {
        return setRightAction(value, true);
    }

    public TextAction setRightAction(Action value, boolean modifyModel) {
        rightAction = value;
        modelModifyingRightAction = modifyModel;
        return this;
    }

    public Action getRightAction() {
        return rightAction;
    }

    public boolean isModelModifyingRightAction() {
        return modelModifyingRightAction;
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
