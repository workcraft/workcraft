package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.workcraft.framework.Framework;

@SuppressWarnings("serial")
public class ErrorView extends JPanel {
	protected PrintStream systemErr;
	protected boolean streamCaptured = false;
	private JScrollPane scrollStdErr;
	private JTextArea txtStdErr;

	class ErrorStreamView extends FilterOutputStream {
		JTextArea target;

		public ErrorStreamView(OutputStream aStream, JTextArea target) {
			super(aStream);
			this.target = target;
		}

		public void puts(String s) {
			this.target.append(s);
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
	}
}
