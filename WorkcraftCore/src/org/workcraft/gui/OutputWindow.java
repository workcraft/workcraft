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
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.shared.CommonLogSettings;
import org.workcraft.util.LogUtils;

@SuppressWarnings("serial")
public class OutputWindow extends JPanel {
    protected PrintStream systemOut;
    protected boolean streamCaptured = false;
    private final JTextArea txtStdOut;

    enum LogType {
        INFO,
        WARNING,
        ERROR,
        STDOUT,
        STDERR,
    }

    public OutputWindow() {
        txtStdOut = new JTextArea();
        txtStdOut.setMargin(SizeHelper.getTextMargin());
        txtStdOut.setLineWrap(true);
        txtStdOut.setEditable(false);
        txtStdOut.setWrapStyleWord(true);
        txtStdOut.addMouseListener(new LogAreaMouseListener());

        DefaultCaret caret = (DefaultCaret) txtStdOut.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollStdOut = new JScrollPane();
        scrollStdOut.setViewportView(txtStdOut);

        setLayout(new BorderLayout());
        add(scrollStdOut, BorderLayout.CENTER);
    }

    public void captureStream() {
        if (!streamCaptured) {
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
        private final JTextArea target;
        private LogType oldType = null;
        private boolean needsNewLine = false;

        OutputStreamView(OutputStream aStream, JTextArea target) {
            super(aStream);
            this.target = target;
        }

        @Override
        public void write(byte[] b) throws IOException {
            if (systemOut != null) {
                systemOut.write(b);
            }
            print(new String(b));
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (systemOut != null) {
                systemOut.write(b, off, len);
            }
            print(new String(b, off, len));
        }

        private void print(String text) {
            Highlighter.HighlightPainter painter = null;
            LogType type = oldType;
            if (LogUtils.isInfoText(text)) {
                type = LogType.INFO;
                painter = new DefaultHighlighter.DefaultHighlightPainter(CommonLogSettings.getInfoBackground());
            } else if (LogUtils.isWarningText(text)) {
                type = LogType.WARNING;
                painter = new DefaultHighlighter.DefaultHighlightPainter(CommonLogSettings.getWarningBackground());
            } else if (LogUtils.isErrorText(text)) {
                type = LogType.ERROR;
                painter = new DefaultHighlighter.DefaultHighlightPainter(CommonLogSettings.getErrorBackground());
            } else if (LogUtils.isStdoutText(text)) {
                type = LogType.STDOUT;
                text = LogUtils.getTextWithoutPrefix(text);
                painter = new DefaultHighlighter.DefaultHighlightPainter(CommonLogSettings.getStdoutBackground());
            } else if (LogUtils.isStderrText(text)) {
                type = LogType.STDERR;
                text = LogUtils.getTextWithoutPrefix(text);
                painter = new DefaultHighlighter.DefaultHighlightPainter(CommonLogSettings.getStderrBackground());
            } else if (!text.equals("\n")) {
                type = null;
                painter = new DefaultHighlighter.DefaultHighlightPainter(target.getBackground());
            }

            if ((oldType != null) && (oldType != type) && needsNewLine) {
                target.append("\n");
            }
            oldType = type;
            needsNewLine = !text.endsWith("\n");

            int fromPos = target.getDocument().getLength();
            target.append(text);
            int toPos = target.getDocument().getLength();
            target.setCaretPosition(toPos);

            Color textColor = CommonLogSettings.getTextColor();
            target.setForeground(textColor);
            target.setFont(new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize()));

            if ((painter != null) && (toPos > fromPos)) {
                try {
                    DefaultHighlighter highlighter = (DefaultHighlighter) target.getHighlighter();
                    highlighter.setDrawsLayeredHighlights(false);
                    highlighter.addHighlight(fromPos, toPos, painter);
                } catch (BadLocationException e) {
                }
            }
        }
    }

}
