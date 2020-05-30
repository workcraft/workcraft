package org.workcraft.gui.actions;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;

@SuppressWarnings("serial")
public class ActionButton extends JButton implements Actor {

    public ActionButton(Icon icon, Action action) {
        super();
        String toolTip = ActionUtils.getActionTooltip(action);
        if (icon == null) {
            setToolTipText(toolTip);
        } else {
            GuiUtils.decorateButton(this, icon, toolTip);
        }
        action.addActor(this);
        setEnabled(action.isEnabled());
        addActionListener(actionEvent -> action.run());
    }

    @Override
    public void actionEnableStateChanged(boolean actionEnableState) {
        setEnabled(actionEnableState);
    }

}
