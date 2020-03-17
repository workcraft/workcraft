package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

public class ActionDeclaration extends PropertyDeclaration<Action> {

    public ActionDeclaration(String description, Runnable runnable) {
        this(null, description, runnable);
    }

    public ActionDeclaration(String name, String description, Runnable runnable) {
        this(name, new Action(description, runnable));
    }

    private ActionDeclaration(String name, Action action) {
        super(Action.class, name, value -> { }, () -> action);
    }

    @Override
    public boolean isSpan() {
        return getName() == null;
    }

}
