package org.workcraft.gui.actions;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class ActionButton extends JButton implements Actor {

    class ActionForwarder implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ActionButton.this.fireActionPerformed();
        }
    }

    private final LinkedList<ScriptedActionListener> listeners = new LinkedList<>();
    private final Action action;

    public ActionButton(Action action) {
        this(action, null);
    }

    public ActionButton(Action action, Icon icon) {
        super();
        this.action = action;
        String toolTip = ActionUtils.getActionTooltip(action);
        if (icon == null) {
            setToolTipText(toolTip);
        } else {
            GuiUtils.decorateButton(this, icon, toolTip);
        }
        action.addActor(this);
        setEnabled(action.isEnabled());
        addActionListener(new ActionForwarder());
    }

    private void fireActionPerformed() {
        if (action != null) {
            for (ScriptedActionListener l : listeners) {
                l.actionPerformed(action);
            }
        }
    }

    public void addScriptedActionListener(ScriptedActionListener listener) {
        listeners.add(listener);
    }

    public void removeScriptedActionListener(ScriptedActionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void actionEnableStateChanged(boolean actionEnableState) {
        this.setEnabled(actionEnableState);
    }

}
