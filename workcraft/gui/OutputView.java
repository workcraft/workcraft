package org.workcraft.gui;

import java.awt.BorderLayout;
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
public class OutputView extends JPanel {
	protected PrintStream systemOut;
	protected boolean streamCaptured = false;
	private JScrollPane scrollStdOut;
	private JTextArea txtStdOut;

	class OutputStreamView extends FilterOutputStream {
		JTextArea target;

		public OutputStreamView(OutputStream aStream, JTextArea target) {
			super(aStream);
			this.target = target;
		}

		@Override
		public void write(byte b[]) throws IOException {
			String s = new String(b);
			OutputView.this.txtStdOut.append(s);
		}

		@Override
		public void write(byte b[], int off, int len) throws IOException {
			String s = new String(b , off , len);
			OutputView.this.txtStdOut.append(s);
		}
	}

	public void captureStream() {
		if (this.streamCaptured)
			return;

		PrintStream outPrintStream = new PrintStream(
				new OutputStreamView(
						new ByteArrayOutputStream(), this.txtStdOut));

		this.systemOut = System.out;

		System.setOut(outPrintStream);

		this.streamCaptured = true;
	}

	public void releaseStream() {
		if (!this.streamCaptured)
			return;

		System.setOut(this.systemOut);
		this.systemOut = null;
		this.streamCaptured = false;
	}

	public OutputView (Framework framework) {
		this.txtStdOut = new JTextArea();
		this.txtStdOut.setLineWrap(true);
		this.txtStdOut.setEditable(false);
		this.txtStdOut.setWrapStyleWord(true);
		this.txtStdOut.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		this.scrollStdOut = new JScrollPane();
		this.scrollStdOut.setViewportView(this.txtStdOut);

		setLayout(new BorderLayout(0,0));
		this.add(this.scrollStdOut, BorderLayout.CENTER);
	}
}
