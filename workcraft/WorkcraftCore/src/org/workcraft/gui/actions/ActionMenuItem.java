package org.workcraft.gui.actions;

import javax.swing.*;

@SuppressWarnings("serial")
public class ActionMenuItem extends JMenuItem implements Actor {

    public ActionMenuItem(Action action) {
        super(action.getTitle());
        action.addActor(this);
        setEnabled(action.isEnabled());
        setAccelerator(action.getKeyStroke());
        addActionListener(actionEvent -> action.run());
    }

    @Override
    public void actionEnableStateChanged(boolean actionEnableState) {
        this.setEnabled(actionEnableState);
    }

}
