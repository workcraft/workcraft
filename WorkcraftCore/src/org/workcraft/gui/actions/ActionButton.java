package org.workcraft.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.JButton;

import org.workcraft.utils.GuiUtils;

@SuppressWarnings("serial")
public class ActionButton extends JButton implements Actor {

    class ActionForwarder implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ActionButton.this.fireActionPerformed();
        }
    }

    private final LinkedList<ScriptedActionListener> listeners = new LinkedList<>();
    private Action action = null;

    public ActionButton(Action action) {
        this(action, action.getText());
    }

    public ActionButton(Action action, String text) {
        super(text);
        String toolTip = ActionUtils.getActionTooltip(action);
        setToolTipText(toolTip);
        this.action = action;
        action.addActor(this);
        setEnabled(action.isEnabled());
        addActionListener(new ActionForwarder());
    }

    public ActionButton(Action action, Icon icon) {
        super();
        String toolTip = ActionUtils.getActionTooltip(action);
        GuiUtils.decorateButton(this, icon, toolTip);
        this.action = action;
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
