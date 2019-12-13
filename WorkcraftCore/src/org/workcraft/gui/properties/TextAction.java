package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

public class TextAction {

    private final String text;
    private final Action action;

    public TextAction(String text, Action action) {
        this.text = text;
        this.action = action;
    }

    public String getText() {
        return text;
    }

    public Action getAction() {
        return action;
    }

}
