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

package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.mozilla.javascript.Context;
import org.syntax.jedit.JEditTextArea;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;
import org.workcraft.Framework;

@SuppressWarnings("serial")
public class JavaScriptWindow extends JPanel {
	private Framework framework;

	private JPanel panelInput = null;
	private JButton btnExecute = null;
	private JEditTextArea txtScript = null;

	public JavaScriptWindow (Framework framework) {
		this.framework = framework;

		btnExecute = new JButton();
		btnExecute.setText("Execute [ctrl-Enter]");

		btnExecute.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				execScript();
			}
		});

		txtScript = new JEditTextArea();
		txtScript.setTokenMarker(new JavaScriptTokenMarker());
		txtScript.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyReleased(java.awt.event.KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown() == true)
					execScript();
			}
		});

		panelInput = new JPanel();
		panelInput.setLayout(new BorderLayout());
		panelInput.add(txtScript, BorderLayout.CENTER);
		//panelInput.add(btnExecute, BorderLayout.SOUTH);
		panelInput.setMinimumSize(new Dimension(100,100));

		setLayout(new BorderLayout());
		this.add(panelInput, BorderLayout.CENTER);

	}


	public void execScript() {
		if (txtScript.getText().length()>0)
			try {
				Object result = framework.execJavaScript(txtScript.getText());

				Context.enter();
				String out = Context.toString(result);
				Context.exit();
				if (!out.equals("undefined"))
					System.out.println (out);
				txtScript.setText("");
			}
		catch (org.mozilla.javascript.WrappedException e) {
			System.err.println(e.getWrappedException().getClass().getName()+" "+e.getWrappedException().getMessage());
		}
		catch (org.mozilla.javascript.RhinoException e) {
			System.err.println(e.getMessage());
		}
	}

}
