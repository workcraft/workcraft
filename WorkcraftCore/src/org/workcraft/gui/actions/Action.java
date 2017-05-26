package org.workcraft.gui.actions;

import java.util.LinkedList;

import javax.swing.KeyStroke;

public abstract class Action/* extends AbstractAction */ {
    private static final long serialVersionUID = -2235480226869966860L;
    private final LinkedList<Actor> actors = new LinkedList<>();
    private boolean enabled = true;

    public abstract String getText();
    public abstract void run();

    void addActor(Actor actor) {
        actors.add(actor);
    }

    void removeActor(Actor actor) {
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
