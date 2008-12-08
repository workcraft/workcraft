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
			this.target.append(s);

			Container parent = 	ErrorView.this.getParent().getParent().getParent();
			if (parent instanceof JTabbedPane) {
				JTabbedPane tab = (JTabbedPane) parent;
				for (int i=0; i<tab.getComponentCount(); i++) {
					if (tab.getComponentAt(i) == ErrorView.this.getParent().getParent()) {
						if (tab.getForegroundAt(i) != Color.RED) {
							colorBack = tab.getForegroundAt(i);
							tab.setForegroundAt(i, Color.RED);
							tab.removeChangeListener(this);
							tab.addChangeListener(this);
						}
					}
				}
				//tab.getTabComponentAt(index)setForeground(Color.RED);
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

			Container parent = 	ErrorView.this.getParent().getParent().getParent();
			if (parent instanceof JTabbedPane) {
				JTabbedPane tab = (JTabbedPane) parent;
				for (int i=0; i<tab.getComponentCount(); i++) {
					if (tab.getComponentAt(i) == ErrorView.this.getParent().getParent()) {
						if (tab.getSelectedComponent() == tab.getComponentAt(i)) {
							tab.setForegroundAt(i, colorBack);
							colorBack = null;
						}
					}
				}
				//tab.getTabComponentAt(index)setForeground(Color.RED);
			}
		}
	}

	public void captureStream() {
		if (this.streamCaptured)
			return;

		PrintStream errPrintStream = new PrintStream(
				new ErrorStreamView(
						new ByteArrayOutputStream(), this.txtStdErr));

		this.systemErr = System.err;

		System.setErr(errPrintStream);

		this.streamCaptured = true;
	}

	public void releaseStream() {
		if (!this.streamCaptured)
			return;

		System.setErr(this.systemErr);
		this.systemErr = null;
		this.streamCaptured = false;
	}

	public ErrorView (Framework framework) {
		this.txtStdErr = new JTextArea();
		this.txtStdErr.setLineWrap(true);
		this.txtStdErr.setEditable(false);
		this.txtStdErr.setWrapStyleWord(true);
		this.txtStdErr.setForeground(Color.RED);
		this.txtStdErr.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		this.scrollStdErr = new JScrollPane();
		this.scrollStdErr.setViewportView(this.txtStdErr);

		setLayout(new BorderLayout(0,0));
		this.add(this.scrollStdErr, BorderLayout.CENTER);

		this.addComponentListener(this);
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

		Container parent = 	this.getParent().getParent().getParent();
		if (parent instanceof JTabbedPane) {
			JTabbedPane tab = (JTabbedPane) parent;
			for (int i=0; i<tab.getComponentCount(); i++) {
				if (tab.getComponentAt(i) == ErrorView.this.getParent().getParent()) {
					tab.setForegroundAt(i, colorBack);
					colorBack = null;
				}
			}
			//tab.getTabComponentAt(index)setForeground(Color.RED);
		}
	}
}
