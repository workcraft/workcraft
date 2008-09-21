package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.workcraft.framework.Framework;

@SuppressWarnings("serial")
public class OutputView extends JPanel {
	protected PrintStream systemOut;
	protected boolean streamCaptured = false;
	private Framework framework;
	private JScrollPane scrollStdOut;
	private JTextArea txtStdOut;

	class OutputStreamView extends FilterOutputStream {
		JTextArea target;

		public OutputStreamView(OutputStream aStream, JTextArea target) {
			super(aStream);
			this.target = target;
		}

		public void write(byte b[]) throws IOException {
			String s = new String(b);
			txtStdOut.append(s);
		}

		public void write(byte b[], int off, int len) throws IOException {
			String s = new String(b , off , len);
			txtStdOut.append(s);
		}
	}

	public void captureStream() {
		if (streamCaptured)
			return;

		PrintStream outPrintStream = new PrintStream(
				new OutputStreamView(
						new ByteArrayOutputStream(), txtStdOut));

		systemOut = System.out;

		System.setOut(outPrintStream);

		streamCaptured = true;
	}

	public void releaseStream() {
		if (!streamCaptured)
			return;

		System.setOut(systemOut);
		systemOut = null;
		streamCaptured = false;
	}

	public OutputView (Framework framework) {
		this.framework = framework;


		txtStdOut = new JTextArea();
		txtStdOut.setLineWrap(true);
		txtStdOut.setEditable(false);
		txtStdOut.setWrapStyleWord(true);
		txtStdOut.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		scrollStdOut = new JScrollPane();
		scrollStdOut.setViewportView(txtStdOut);

		this.setLayout(new BorderLayout(0,0));
		this.add(scrollStdOut, BorderLayout.CENTER);
	}
}
