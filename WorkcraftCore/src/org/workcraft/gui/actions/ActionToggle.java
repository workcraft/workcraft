package org.workcraft.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.JToggleButton;

import org.workcraft.utils.GuiUtils;

@SuppressWarnings("serial")
public class ActionToggle extends JToggleButton implements Actor {

    class ActionForwarder implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ActionToggle.this.fireActionPerformed();
        }
    }

    private final LinkedList<ScriptedActionListener> listeners = new LinkedList<>();
    private Action action = null;

    public ActionToggle(Action action) {
        this(action, action.getText());
    }

    public ActionToggle(Action action, String text) {
        super(text);
        this.action = action;
        action.addActor(this);
        setEnabled(action.isEnabled());
        addActionListener(new ActionForwarder());
    }

    public ActionToggle(Action action, Icon icon) {
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
