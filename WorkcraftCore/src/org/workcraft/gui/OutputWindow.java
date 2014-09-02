/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

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

import org.workcraft.Framework;

@SuppressWarnings("serial")
public class OutputWindow extends JPanel {
	protected PrintStream systemOut;
	protected boolean streamCaptured = false;
	private JScrollPane scrollStdOut;
	private JTextArea txtStdOut;

	public OutputWindow(Framework framework) {
		txtStdOut = new JTextArea();
		txtStdOut.setLineWrap(true);
		txtStdOut.setEditable(false);
		txtStdOut.setWrapStyleWord(true);
		txtStdOut.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		txtStdOut.addMouseListener(new LogAreaMouseListener());

		scrollStdOut = new JScrollPane();
		scrollStdOut.setViewportView(txtStdOut);

		setLayout(new BorderLayout(0,0));
		this.add(scrollStdOut, BorderLayout.CENTER);
	}

	class OutputStreamView extends FilterOutputStream {
		JTextArea target;

		public OutputStreamView(OutputStream aStream, JTextArea target) {
			super(aStream);
			this.target = target;
		}

		@Override
		public void write(byte b[]) throws IOException {
			systemOut.write(b);
			String s = new String(b);
			txtStdOut.append(s);
			txtStdOut.setCaretPosition(txtStdOut.getDocument().getLength());
		}

		@Override
		public void write(byte b[], int off, int len) throws IOException {
			systemOut.write(b, off, len);
			String s = new String(b , off , len);
			txtStdOut.append(s);
		}
	}

	public void captureStream() {
		if (!streamCaptured) {
			PrintStream outPrintStream = new PrintStream(new OutputStreamView(
					new ByteArrayOutputStream(), txtStdOut));

			systemOut = System.out;
			System.setOut(outPrintStream);
			streamCaptured = true;
		}
	}

	public void releaseStream() {
		if (streamCaptured) {
			System.setOut(systemOut);
			systemOut = null;
			streamCaptured = false;
		}
	}

}
