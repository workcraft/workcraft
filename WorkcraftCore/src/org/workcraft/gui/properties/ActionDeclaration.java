package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

import java.util.Map;

public class ActionDeclaration implements PropertyDescriptor<Action> {

    private final Action action;

    public ActionDeclaration(String text, Runnable runnable) {
        this.action = new Action(text, runnable);
    }

    @Override
    public Map<Action, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Class<Action> getType() {
        return Action.class;
    }

    @Override
    public final Action getValue() {
        return action;
    }

    @Override
    public void setValue(Action value) {
    }

    @Override
    public boolean isSpan() {
        return true;
    }

}
