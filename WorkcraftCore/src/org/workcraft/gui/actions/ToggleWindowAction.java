package org.workcraft.gui.actions;

import org.workcraft.Framework;
import org.workcraft.gui.tabs.DockableWindow;

public class ToggleWindowAction extends Action {
    private final DockableWindow window;

    public ToggleWindowAction(DockableWindow window) {
        this.window = window;
    }

    @Override
    public String getText() {
        return window.getTitle();
    }

    @Override
    public void run() {
        final Framework framework = Framework.getInstance();
        framework.getMainWindow().toggleDockableWindow(window);
    }

}
