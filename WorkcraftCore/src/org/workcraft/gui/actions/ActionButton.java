/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;

@SuppressWarnings("serial")
public class ActionButton extends JButton implements Actor {
    class ActionForwarder implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            ActionButton.this.fireActionPerformed();
        }
    }

    private final LinkedList<ScriptedActionListener> listeners = new LinkedList<>();
    private Action action = null;

    public ActionButton(Action action, String text) {
        super(text);
        this.action = action;
        action.addActor(this);
        setEnabled(action.isEnabled());

        addActionListener(new ActionForwarder());
    }

    public ActionButton(Action action) {
        this(action, action.getText());

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

    public void actionEnableStateChanged(boolean actionEnableState) {
        this.setEnabled(actionEnableState);
    }
}
