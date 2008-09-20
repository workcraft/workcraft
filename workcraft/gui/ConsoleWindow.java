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

public class ConsoleWindow extends InternalWindow {

	public void startup() {



		scrollErrors = new JScrollPane();
		scrollErrors.setViewportView(txtStdErr);

		scrollActions = new JScrollPane();
		scrollActions.setViewportView(txtActions);

		scrollInput = new JScrollPane();
		scrollInput.setViewportView(txtScript);


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
