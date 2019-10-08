package org.workcraft.plugins.xbm.properties;

import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.PropertyDescriptor;

import java.util.Map;

public class CreateSignalPropertyDescriptor implements PropertyDescriptor<Action> {

    private final Action action;

    public CreateSignalPropertyDescriptor(String text, Runnable runnable) {
        this.action = new Action(text, runnable);
    }

    @Override
    public Map<Action, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return action.getText();
    }

    @Override
    public Class<Action> getType() {
        return Action.class;
    }

    @Override
    public Action getValue() {
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
