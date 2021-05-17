package org.workcraft.gui;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.controls.LogPanel;
import org.workcraft.plugins.builtin.settings.LogCommonSettings;
import org.workcraft.utils.HighlightUtils;
import org.workcraft.utils.LogUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;

@SuppressWarnings("serial")
public class OutputWindow extends LogPanel {

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
            displayInEventDispatchThread(new String(b));
        }

        @Override
        public void write(byte[] b, int off, int len) {
            if (systemOut != null) {
                systemOut.write(b, off, len);
            }
            displayInEventDispatchThread(new String(b, off, len));
        }

        private void displayInEventDispatchThread(String text) {
            SwingUtilities.invokeLater(() -> displayThreadUnsafe(text));
        }

        private void displayThreadUnsafe(String text) {
            LogType type = oldType;
            Color highlightColor = null;
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

            HighlightUtils.highlightLines(target, fromPos, toPos, highlightColor);
        }
    }

    private PrintStream systemOut;
    private boolean streamCaptured = false;

    enum LogType {
        INFO,
        WARNING,
        ERROR,
        STDOUT,
        STDERR,
    }

    public void captureStream() {
        if (!streamCaptured) {
            OutputStreamView outView = new OutputStreamView(new ByteArrayOutputStream(), getTextArea());
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

}
