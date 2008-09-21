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
	private Framework framework;
	private JScrollPane scrollStdErr;
	private JTextArea txtStdErr;

	class ErrorStreamView extends FilterOutputStream {
		JTextArea target;

		public ErrorStreamView(OutputStream aStream, JTextArea target) {
			super(aStream);
			this.target = target;
		}

		public void puts(String s) {
			target.append(s);
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

		System.setOut(systemErr);
		systemErr = null;
		streamCaptured = false;
	}

	public ErrorView (Framework framework) {
		this.framework = framework;

		txtStdErr = new JTextArea();
		txtStdErr.setLineWrap(true);
		txtStdErr.setEditable(false);
		txtStdErr.setWrapStyleWord(true);
		txtStdErr.setForeground(Color.RED);
		txtStdErr.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		scrollStdErr = new JScrollPane();
		scrollStdErr.setViewportView(txtStdErr);

		this.setLayout(new BorderLayout(0,0));
		this.add(scrollStdErr, BorderLayout.CENTER);
	}
}
