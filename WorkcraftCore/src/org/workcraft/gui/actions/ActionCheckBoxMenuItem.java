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

import javax.swing.JCheckBoxMenuItem;

@SuppressWarnings("serial")
public class ActionCheckBoxMenuItem extends JCheckBoxMenuItem implements Actor {
    class ActionForwarder implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            ActionCheckBoxMenuItem.this.fireActionPerformed();
        }
    }

    private LinkedList<ScriptedActionListener> listeners = new LinkedList<ScriptedActionListener>();
    private Action scriptedAction = null;

    public ActionCheckBoxMenuItem(Action action) {
        this(action, action.getText());
    }

    public ActionCheckBoxMenuItem(Action action, String text) {
        super(text);
        scriptedAction = action;
        scriptedAction.addActor(this);
        setEnabled(scriptedAction.isEnabled());

        addActionListener(new ActionForwarder());
    }

    private void fireActionPerformed() {
        if (scriptedAction != null) {
            for (ScriptedActionListener l : listeners) {
                l.actionPerformed(scriptedAction);
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