package org.workcraft.gui.actions;

import javax.swing.JToolBar;

public class ToggleToolbarAction extends Action {
    private final JToolBar toolbar;

    public ToggleToolbarAction(JToolBar toolbar) {
        this.toolbar = toolbar;
    }

    @Override
    public String getText() {
        return toolbar.getName();
    }

    @Override
    public void run() {
        boolean visible = toolbar.isVisible();
        toolbar.setVisible(!visible);
    }

}
