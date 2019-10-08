package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;
import org.workcraft.types.Pair;

public class TextAction extends Pair<String, Action> {

    public TextAction(String text, Action action) {
        super(text, action);
    }

    public String getText() {
        return getFirst();
    }

    public Action getAction() {
        return getSecond();
    }

}
