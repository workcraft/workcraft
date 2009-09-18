package org.workcraft.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;

@SuppressWarnings("serial")
public class ScriptedActionButton extends JButton implements ScriptedActor {
	class ActionForwarder implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ScriptedActionButton.this.fireActionPerformed();
		}
	}

	private LinkedList<ScriptedActionListener> listeners = new LinkedList<ScriptedActionListener>();
	private ScriptedAction scriptedAction = null;

	public ScriptedActionButton(ScriptedAction action, String text) {
		super(text);
		scriptedAction = action;
		scriptedAction.addActor(this);
		setEnabled(scriptedAction.isEnabled());

		addActionListener(new ActionForwarder());
	}

	public ScriptedActionButton(ScriptedAction action) {
		this(action, action.getText());

	}

	private void fireActionPerformed() {
		if (scriptedAction != null)
			for (ScriptedActionListener l : listeners)
				l.actionPerformed(scriptedAction);
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
