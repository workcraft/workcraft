package org.workcraft.gui.actions;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class ActionMenuItem extends JMenuItem implements Actor {

    class ActionForwarder implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ActionMenuItem.this.fireActionPerformed();
        }
    }

    private final LinkedList<ScriptedActionListener> listeners = new LinkedList<>();
    private Action action = null;

    public ActionMenuItem(Action action) {
        this(action.getTitle(), action);
    }

    public ActionMenuItem(String text, Action action) {
        super(text);
        this.action = action;
        this.action.addActor(this);
        setEnabled(this.action.isEnabled());
        setAccelerator(action.getKeyStroke());
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
