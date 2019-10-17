package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

import java.util.Map;

public class ActionDeclaration implements PropertyDescriptor<Action> {

    private final String name;
    private final Action action;

    public ActionDeclaration(String text, Runnable runnable) {
        this(null, text, runnable);
    }

    public ActionDeclaration(String name, String text, Runnable runnable) {
        this(name, new Action(text, runnable));
    }

    public ActionDeclaration(String name, Action action) {
        this.name = name;
        this.action = action;
    }

    @Override
    public Map<Action, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return name;
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
    public boolean isEditable() {
        return true;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public boolean isCombinable() {
        return false;
    }

    @Override
    public boolean isTemplatable() {
        return false;
    }

    @Override
    public boolean isSpan() {
        return name == null;
    }

}
