package org.workcraft.gui.actions;

import javax.swing.*;

@SuppressWarnings("serial")
public class ActionCheckBoxMenuItem extends JCheckBoxMenuItem implements Actor {

    public ActionCheckBoxMenuItem(Action action) {
        super(action.getTitle());
        action.addActor(this);
        setEnabled(action.isEnabled());
        addActionListener(actionEvent -> action.run());
    }

    @Override
    public void actionEnableStateChanged(boolean actionEnableState) {
        this.setEnabled(actionEnableState);
    }

}
