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
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import org.workcraft.plugins.shared.CommonLogSettings;
import org.workcraft.util.LogUtils;

@SuppressWarnings("serial")
public class OutputWindow extends JPanel {
	protected PrintStream systemOut;
	protected boolean streamCaptured = false;
	private JScrollPane scrollStdOut;
	private JTextArea txtStdOut;

	public OutputWindow() {
		txtStdOut = new JTextArea();
		txtStdOut.setLineWrap(true);
		txtStdOut.setEditable(false);
		txtStdOut.setWrapStyleWord(true);
		txtStdOut.addMouseListener(new LogAreaMouseListener());

		DefaultCaret caret = (DefaultCaret)txtStdOut.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		scrollStdOut = new JScrollPane();
		scrollStdOut.setViewportView(txtStdOut);
		setLayout(new BorderLayout(0,0));
		this.add(scrollStdOut, BorderLayout.CENTER);
	}

	public void captureStream() {
		if ( !streamCaptured ) {
			OutputStreamView outView = new OutputStreamView(new ByteArrayOutputStream(), txtStdOut);
			PrintStream outStream = new PrintStream(outView);
			systemOut = System.out;
			System.setOut(outStream);
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

	class OutputStreamView extends FilterOutputStream {
		private JTextArea target;

		public OutputStreamView(OutputStream aStream, JTextArea target) {
			super(aStream);
			this.target = target;
		}

		@Override
		public void write(byte b[]) throws IOException {
			if (systemOut != null) {
				systemOut.write(b);
			}
			print(new String(b));
		}

		@Override
		public void write(byte b[], int off, int len) throws IOException {
			if (systemOut != null) {
				systemOut.write(b, off, len);
			}
			print(new String(b , off , len));
		}

		private void print(String text) {
			Highlighter.HighlightPainter highlighter = null;
			if (text.startsWith(LogUtils.PREFIX_INFO)) {
				highlighter = new DefaultHighlighter.DefaultHighlightPainter(CommonLogSettings.getInfoBackground());
			} else if (text.startsWith(LogUtils.PREFIX_WARNING)) {
				highlighter = new DefaultHighlighter.DefaultHighlightPainter(CommonLogSettings.getWarningBackground());
			} else if (text.startsWith(LogUtils.PREFIX_ERROR)) {
				highlighter = new DefaultHighlighter.DefaultHighlightPainter(CommonLogSettings.getErrorBackground());
			} else if (text.startsWith(LogUtils.PREFIX_STDOUT)) {
				text = text.substring(LogUtils.PREFIX_STDOUT.length());
				highlighter = new DefaultHighlighter.DefaultHighlightPainter(CommonLogSettings.getStdoutBackground());
			} else if (text.startsWith(LogUtils.PREFIX_STDERR)) {
				text = text.substring(LogUtils.PREFIX_STDERR.length());
				highlighter = new DefaultHighlighter.DefaultHighlightPainter(CommonLogSettings.getStderrBackground());
			}
			String suffix = "";
			if (highlighter != null) {
				// A text suffix to separate highlighted portions
				suffix = " ";
			}
			int fromPos = target.getDocument().getLength();
			target.append(text + suffix);
			int toPos = target.getDocument().getLength();
			target.setCaretPosition(toPos);

			target.setForeground(CommonLogSettings.getTextColor());
			target.setFont(new Font(Font.MONOSPACED, Font.PLAIN, CommonLogSettings.getTextSize()));

			if ((highlighter != null) && (toPos > fromPos)) {
				try {
					target.getHighlighter().addHighlight(fromPos, toPos-suffix.length(), highlighter);
				} catch (BadLocationException e) {
				}
			}
		}
	}

}
