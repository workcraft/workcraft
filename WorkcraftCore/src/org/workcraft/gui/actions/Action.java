package org.workcraft.gui.actions;

import javax.swing.*;
import java.util.LinkedList;

public class Action implements Runnable {

    private final Runnable runnable;
    private final String text;
    private final KeyStroke keyStroke;
    private final LinkedList<Actor> actors = new LinkedList<>();
    private boolean enabled = true;

    public Action(Runnable runnable) {
        this(null, null, runnable);
    }

    public Action(String text, Runnable runnable) {
        this(text, null, runnable);
    }

    public Action(String text, KeyStroke keyStroke, Runnable runnable) {
        this.text = text;
        this.keyStroke = keyStroke;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        if (runnable != null) {
            runnable.run();
        }
    }

    public final String getText() {
        return text;
    }

    public KeyStroke getKeyStroke() {
        return keyStroke;
    }

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

}
