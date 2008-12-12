package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.workcraft.framework.Framework;

@SuppressWarnings("serial")
public class ErrorView extends JPanel implements ComponentListener {
	protected PrintStream systemErr;
	protected boolean streamCaptured = false;
	private JScrollPane scrollStdErr;
	private JTextArea txtStdErr;

	private Color colorBack = null;

	class ErrorStreamView extends FilterOutputStream implements ChangeListener {
		JTextArea target;

		public ErrorStreamView(OutputStream aStream, JTextArea target) {
			super(aStream);
			this.target = target;
		}

		public void puts(String s) {
			target.append(s);

			Container parent = 	getParent().getParent().getParent();
			if (parent instanceof JTabbedPane) {
				JTabbedPane tab = (JTabbedPane) parent;
				for (int i=0; i<tab.getComponentCount(); i++)
					if (tab.getComponentAt(i) == getParent().getParent())
						if (tab.getForegroundAt(i) != Color.RED) {
							colorBack = tab.getForegroundAt(i);
							tab.setForegroundAt(i, Color.RED);
							tab.removeChangeListener(this);
							tab.addChangeListener(this);
						}
			}
		}

		@Override
		public void write(byte b[]) throws IOException {
			String s = new String(b);
			puts(s);
		}

		@Override
		public void write(byte b[], int off, int len) throws IOException {
			String s = new String(b , off , len);
			puts(s);
		}

		public void stateChanged(ChangeEvent e) {
			if (colorBack == null)
				return;

			Container parent = 	getParent().getParent().getParent();
			if (parent instanceof JTabbedPane) {
				JTabbedPane tab = (JTabbedPane) parent;
				for (int i=0; i<tab.getComponentCount(); i++)
					if (tab.getComponentAt(i) == getParent().getParent())
						if (tab.getSelectedComponent() == tab.getComponentAt(i)) {
							tab.setForegroundAt(i, colorBack);
							colorBack = null;
						}
			}
		}
	}

	public void captureStream() {
		if (streamCaptured)
			return;

		PrintStream errPrintStream = new PrintStream(
				new ErrorStreamView(
						new ByteArrayOutputStream(), txtStdErr));

		systemErr = System.err;

		System.setErr(errPrintStream);

		streamCaptured = true;
	}

	public void releaseStream() {
		if (!streamCaptured)
			return;

		System.setErr(systemErr);
		systemErr = null;
		streamCaptured = false;
	}

	public ErrorView (Framework framework) {
		txtStdErr = new JTextArea();
		txtStdErr.setLineWrap(true);
		txtStdErr.setEditable(false);
		txtStdErr.setWrapStyleWord(true);
		txtStdErr.setForeground(Color.RED);
		txtStdErr.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		scrollStdErr = new JScrollPane();
		scrollStdErr.setViewportView(txtStdErr);

		setLayout(new BorderLayout(0,0));
		this.add(scrollStdErr, BorderLayout.CENTER);

		addComponentListener(this);
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
		if (colorBack==null)
			return;

		Container parent = 	getParent().getParent().getParent();
		if (parent instanceof JTabbedPane) {
			JTabbedPane tab = (JTabbedPane) parent;
			for (int i=0; i<tab.getComponentCount(); i++)
				if (tab.getComponentAt(i) == ErrorView.this.getParent().getParent()) {
					tab.setForegroundAt(i, colorBack);
					colorBack = null;
				}
		}
	}
}
