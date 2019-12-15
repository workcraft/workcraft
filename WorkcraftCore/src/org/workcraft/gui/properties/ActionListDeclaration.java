package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

public class ActionListDeclaration extends PropertyDeclaration<ActionList> {

    private final ActionList actions = new ActionList();

    public ActionListDeclaration(String name) {
        super(ActionList.class, name, value -> { }, () -> null);
    }

    public ActionListDeclaration addAction(String title, Runnable runnable, String description) {
        actions.add(new Action(title, runnable, description));
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
