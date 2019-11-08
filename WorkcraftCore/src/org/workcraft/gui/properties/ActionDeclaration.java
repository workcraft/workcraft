package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

public class ActionDeclaration extends PropertyDeclaration<Action> {

    public ActionDeclaration(String text, Runnable runnable) {
        this(null, text, runnable);
    }

    public ActionDeclaration(String name, String text, Runnable runnable) {
        this(name, new Action(text, runnable));
    }

    public ActionDeclaration(String name, Action action) {
        super(Action.class, name, value -> { }, () -> action);
    }

    @Override
    public boolean isSpan() {
        return getName() == null;
    }

}
