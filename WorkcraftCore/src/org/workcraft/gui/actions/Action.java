package org.workcraft.gui.actions;

import javax.swing.*;
import java.util.LinkedList;

public class Action implements Runnable {

    private final String title;
    private final Runnable runnable;
    private final KeyStroke keyStroke;
    private final String description;
    private final LinkedList<Actor> actors = new LinkedList<>();
    private boolean enabled = true;

    public Action(String title, Runnable runnable) {
        this(title, runnable, null, null);
    }

    public Action(String title, Runnable runnable, KeyStroke keyStroke) {
        this(title, runnable, keyStroke, null);
    }

    public Action(String title, Runnable runnable, String description) {
        this(title, runnable, null, description);
    }

    public Action(String title, Runnable runnable, KeyStroke keyStroke, String description) {
        this.title = title;
        this.keyStroke = keyStroke;
        this.runnable = runnable;
        this.description = description;
    }

    @Override
    public void run() {
        if (runnable != null) {
            runnable.run();
        }
    }

    public String getTitle() {
        return title;
    }

    public KeyStroke getKeyStroke() {
        return keyStroke;
    }

    public String getDescription() {
        return description;
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
