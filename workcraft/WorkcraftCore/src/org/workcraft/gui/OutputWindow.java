package org.workcraft.gui;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.builtin.settings.LogCommonSettings;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.PopupUtils;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.*;

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
        PopupUtils.setTextAreaPopup(txtStdOut);

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
            Color highlightColor = null;
            LogType type = oldType;
            if (LogUtils.isInfoText(text)) {
                type = LogType.INFO;
                highlightColor = LogCommonSettings.getInfoBackground();
            } else if (LogUtils.isWarningText(text)) {
                type = LogType.WARNING;
                highlightColor = LogCommonSettings.getWarningBackground();
            } else if (LogUtils.isErrorText(text)) {
                type = LogType.ERROR;
                highlightColor = LogCommonSettings.getErrorBackground();
            } else if (LogUtils.isStdoutText(text)) {
                type = LogType.STDOUT;
                text = LogUtils.getTextWithoutPrefix(text);
                highlightColor = LogCommonSettings.getStdoutBackground();
            } else if (LogUtils.isStderrText(text)) {
                type = LogType.STDERR;
                text = LogUtils.getTextWithoutPrefix(text);
                highlightColor = LogCommonSettings.getStderrBackground();
            } else if (!"\n".equals(text)) {
                type = null;
                highlightColor = target.getBackground();
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

            Color textColor = LogCommonSettings.getTextColor();
            target.setForeground(textColor);
            target.setFont(new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize()));

            GuiUtils.highlightLines(target, fromPos, toPos, highlightColor);
        }
    }

}
