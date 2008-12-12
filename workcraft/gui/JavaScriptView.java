package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.mozilla.javascript.Context;
import org.syntax.jedit.JEditTextArea;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;
import org.workcraft.framework.Framework;

@SuppressWarnings("serial")
public class JavaScriptView extends JPanel {
	private Framework framework;

	private JPanel panelInput = null;
	private JButton btnExecute = null;
	private JEditTextArea txtScript = null;

	public JavaScriptView (Framework framework) {
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
