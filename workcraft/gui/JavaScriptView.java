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

		this.btnExecute = new JButton();
		this.btnExecute.setText("Execute [ctrl-Enter]");

		this.btnExecute.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				execScript();
			}
		});

		this.txtScript = new JEditTextArea();
		this.txtScript.setTokenMarker(new JavaScriptTokenMarker());
		this.txtScript.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyReleased(java.awt.event.KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown() == true)
					execScript();
			}
		});

		this.panelInput = new JPanel();
		this.panelInput.setLayout(new BorderLayout());
		this.panelInput.add(this.txtScript, BorderLayout.CENTER);
		//panelInput.add(btnExecute, BorderLayout.SOUTH);
		this.panelInput.setMinimumSize(new Dimension(100,100));

		setLayout(new BorderLayout());
		this.add(this.panelInput, BorderLayout.CENTER);

	}


	public void execScript() {
		if (this.txtScript.getText().length()>0)
			try {
				Object result = this.framework.execJavaScript(this.txtScript.getText());

				Context.enter();
				String out = Context.toString(result);
				Context.exit();
				if (!out.equals("undefined"))
					System.out.println (out);
				this.txtScript.setText("");
			}
		catch (org.mozilla.javascript.WrappedException e) {
			System.err.println(e.getWrappedException().getClass().getName()+" "+e.getWrappedException().getMessage());
		}
		catch (org.mozilla.javascript.RhinoException e) {
			System.err.println(e.getMessage());
		}
	}

}
