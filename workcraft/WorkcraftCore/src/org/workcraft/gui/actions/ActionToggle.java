package org.workcraft.gui.actions;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;

@SuppressWarnings("serial")
public class ActionToggle extends JToggleButton implements Actor {

    public ActionToggle(Icon icon, Action action) {
        super();
        String toolTip = ActionUtils.getActionTooltip(action);
        GuiUtils.decorateButton(this, icon, toolTip);
        action.addActor(this);
        setEnabled(action.isEnabled());
        addActionListener(actionEvent -> action.run());
    }

    @Override
    public void actionEnableStateChanged(boolean actionEnableState) {
        this.setEnabled(actionEnableState);
    }

}
