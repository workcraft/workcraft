package org.workcraft.gui.actions;

import javax.swing.*;
import java.util.LinkedList;

public abstract class Action implements Runnable {

    private final LinkedList<Actor> actors = new LinkedList<>();
    private boolean enabled = true;

    public abstract String getText();

    public void addActor(Actor actor) {
        actors.add(actor);
    }

    public void removeActor(Actor actor) {
        actors.remove(actor);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            for (Actor actor : actors) {
                actor.actionEnableStateChanged(enabled);
            }
        }
    }
    public boolean isEnabled() {
        return enabled;
    }

    public KeyStroke getKeyStroke() {
        return null;
    }

}
