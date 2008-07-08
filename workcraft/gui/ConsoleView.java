package org.workcraft.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JInternalFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Point;

import javax.swing.JTextArea;
import java.awt.GridBagConstraints;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JButton;

import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import org.mozilla.javascript.Context;
import org.syntax.jedit.JEditTextArea;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;
import org.syntax.jedit.tokenmarker.TokenMarker;
import org.workcraft.framework.Framework;


import java.awt.Font;
import java.awt.event.KeyEvent;

public class ConsoleView extends MDIDocumentFrame {
	private static final long serialVersionUID = 1L;
	protected PrintStream systemOut, systemErr;
	protected boolean streamsCaptured = false;

	class ConsoleOutputStream extends FilterOutputStream {
		JTextArea target;

		public ConsoleOutputStream(OutputStream aStream, JTextArea target) {
			super(aStream);
			this.target = target;
		}

		public void write(byte b[]) throws IOException {
			String s = new String(b);
			target.append(s);
		}

		public void write(byte b[], int off, int len) throws IOException {
			String s = new String(b , off , len);
			target.append(s);
		}
	}
	class ErrorOutputStream extends FilterOutputStream {
		JTextArea target;

		public ErrorOutputStream(OutputStream aStream, JTextArea target) {
			super(aStream);
			this.target = target;
		}

		public void puts(String s) {
			target.append(s);
			if (consoleTabbedPane.getSelectedIndex()!=1) {
				consoleTabbedPane.setForegroundAt(1, Color.RED);
			}
		}

		public void write(byte b[]) throws IOException {
			String s = new String(b);
			puts(s);
		}

		public void write(byte b[], int off, int len) throws IOException {
			String s = new String(b , off , len);
			puts(s);
		}
	}

	private PrintStream outPrintStream;
	private PrintStream errPrintStream;
	private Framework framework;

	private JTabbedPane consoleTabbedPane = null;
	private JPanel panelStdOut = null;
	private JSplitPane splitConsole = null;
	private JScrollPane scrollStdOut = null;
	private JTextArea txtStdOut = null;
	private JPanel panelInput = null;
	private JScrollPane scrollInput = null;
	private JButton btnExecute = null;
	private JEditTextArea txtScript = null;
	private JScrollPane scrollErrors = null;
	private JTextArea txtStdErr = null;
	private JScrollPane scrollActions = null;
	private JTextArea txtActions = null;

	public ConsoleView(Framework framework) {
		super("Console");
		this.framework = framework;
	}



	public void captureStreams() {
		if (streamsCaptured)
			return;

		outPrintStream = new PrintStream(
				new ConsoleOutputStream(
						new ByteArrayOutputStream(), txtStdOut));
		errPrintStream = new PrintStream(
				new ErrorOutputStream(
						new ByteArrayOutputStream(), txtStdErr));

		systemOut = System.out;
		systemErr = System.err;

		System.setOut(outPrintStream);
		System.setErr(errPrintStream);

		streamsCaptured = true;
	}

	public void releaseStreams() {
		if (!streamsCaptured)
			return;

		System.setOut(systemOut);
		System.setErr(systemErr);

		systemOut = null;
		systemErr = null;

		streamsCaptured = false;
	}

	public void execScript() {
		if (txtScript.getText().length()>0) {
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

	public void startup() {
		btnExecute = new JButton();
		btnExecute.setText("Execute [ctrl-Enter]");
		btnExecute.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				execScript();
			}
		});

		txtStdOut = new JTextArea();
		txtStdOut.setLineWrap(true);
		txtStdOut.setEditable(false);
		txtStdOut.setWrapStyleWord(true);

		txtActions = new JTextArea();
		txtActions.setLineWrap(true);
		txtActions.setWrapStyleWord(true);
		txtActions.setEditable(false);

		txtStdErr = new JTextArea();
		txtStdErr.setLineWrap(true);
		txtStdErr.setWrapStyleWord(true);
		txtStdErr.setEditable(false);

		txtScript = new JEditTextArea();
		txtScript.setTokenMarker(new JavaScriptTokenMarker());
		txtScript.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(java.awt.event.KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown() == true)
					execScript();
			}
		});

		scrollStdOut = new JScrollPane();
		scrollStdOut.setViewportView(txtStdOut);

		scrollErrors = new JScrollPane();
		scrollErrors.setViewportView(txtStdErr);

		scrollActions = new JScrollPane();
		scrollActions.setViewportView(txtActions);

		scrollInput = new JScrollPane();
		scrollInput.setViewportView(txtScript);

		panelInput = new JPanel();
		panelInput.setLayout(new BorderLayout());
		panelInput.setBorder(BorderFactory.createTitledBorder(null, "JavaScript", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));
		panelInput.add(txtScript, BorderLayout.CENTER);
		panelInput.add(btnExecute, BorderLayout.SOUTH);
		panelInput.setMinimumSize(new Dimension(100,100));

		splitConsole = new JSplitPane();
		splitConsole.setDividerSize(4);
		splitConsole.setLeftComponent(scrollStdOut);
		splitConsole.setRightComponent(panelInput);

		panelStdOut = new JPanel();
		panelStdOut.setLayout(new BorderLayout());
		panelStdOut.add(splitConsole, BorderLayout.CENTER);

		consoleTabbedPane = new JTabbedPane();
		consoleTabbedPane.addTab("Output", null, panelStdOut, null);
		consoleTabbedPane.addTab("Errors", null, scrollErrors, null);
		consoleTabbedPane.addTab("Actions", null, scrollActions, null);

		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setContentPane(consoleTabbedPane);
		this.setTitle("Console");

		this.setLocation(framework.getConfigVarAsInt("gui.console.x", 0), framework.getConfigVarAsInt("gui.console.y", 0));
		this.setSize(framework.getConfigVarAsInt("gui.console.width", 500), framework.getConfigVarAsInt("gui.console.height", 300));
		this.splitConsole.setDividerLocation(framework.getConfigVarAsInt("gui.console.divider", 300));

	}

	public void shutdown() {
		framework.setConfigVar("gui.console.x", this.getX());
		framework.setConfigVar("gui.console.y", this.getY());
		framework.setConfigVar("gui.console.width", this.getWidth());
		framework.setConfigVar("gui.console.height", this.getHeight());
		framework.setConfigVar("gui.console.divider", splitConsole.getDividerLocation());
	}

}
