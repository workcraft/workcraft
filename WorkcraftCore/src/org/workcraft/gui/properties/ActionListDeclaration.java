package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

import java.util.Map;

public class ActionListDeclaration implements PropertyDescriptor<ActionList> {

    private final String name;
    private final ActionList actions = new ActionList();

    public ActionListDeclaration() {
        this(null);
    }

    public ActionListDeclaration(String name) {
        this.name = name;
    }

    public ActionListDeclaration addAction(String text, Runnable runnable) {
        actions.add(new Action(text, runnable));
        return this;
    }

    @Override
    public Map<ActionList, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<ActionList> getType() {
        return ActionList.class;
    }

    @Override
    public final ActionList getValue() {
        return actions;
    }

    @Override
    public void setValue(ActionList value) {
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
