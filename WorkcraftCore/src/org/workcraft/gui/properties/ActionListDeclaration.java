package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

public class ActionListDeclaration extends PropertyDeclaration<ActionList> {

    private final ActionList actions = new ActionList();

    public ActionListDeclaration() {
        this(null);
    }

    public ActionListDeclaration(String name) {
        super(ActionList.class, name, (value) -> { }, () -> null);
    }

    public ActionListDeclaration addAction(String text, Runnable runnable) {
        actions.add(new Action(text, runnable));
        return this;
    }

    @Override
    public final ActionList getValue() {
        return actions;
    }

    @Override
    public boolean isSpan() {
        return getName() == null;
    }

}
