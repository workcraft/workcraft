package org.workcraft.gui.panels;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.controls.TextEditor;
import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.plugins.builtin.settings.LogCommonSettings;
import org.workcraft.types.Pair;
import org.workcraft.utils.HighlightUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import static org.workcraft.utils.LogUtils.getTextWithoutPrefix;

public class OutputLogPanel extends LogPanel {

    private final ArrayList<LogType> lineLogType = new ArrayList<>();

    class OutputStreamView extends FilterOutputStream {
        private final JTextArea target;
        private LogType oldLogType = null;
        private boolean targetHasNewLine = false;
        private Instant startTime = Instant.now();

        OutputStreamView(OutputStream aStream, JTextArea target) {
            super(aStream);
            this.target = target;

            if (DebugCommonSettings.getLogPerformance()) {
                addDebugMenu();
            }
        }

        private void addDebugMenu() {
            JPopupMenu popup = target.getComponentPopupMenu();
            popup.addSeparator();

            JMenuItem miAddLines = new JMenuItem("Add 1k lines");
            miAddLines.addActionListener(event -> addLines(100));
            miAddLines.setMnemonic(KeyEvent.VK_L);
            popup.add(miAddLines);

            JMenuItem miAdd9kLines = new JMenuItem("Add 9k lines");
            miAdd9kLines.addActionListener(event -> addLines(900));
            miAdd9kLines.setMnemonic(KeyEvent.VK_K);
            popup.add(miAdd9kLines);

            JMenuItem miPrintTime = new JMenuItem("Print duration");
            miPrintTime.addActionListener(event -> printDuration());
            miPrintTime.setMnemonic(KeyEvent.VK_D);
            popup.add(miPrintTime);
        }

        @Override
        public void write(byte[] b) throws IOException {
            if (systemOut != null) {
                systemOut.write(b);
            }
            displayInEventDispatchThread(new String(b, StandardCharsets.UTF_8));
        }

        @Override
        public void write(byte[] b, int off, int len) {
            if (systemOut != null) {
                systemOut.write(b, off, len);
            }
            displayInEventDispatchThread(new String(b, off, len, StandardCharsets.UTF_8));
        }

        private void displayInEventDispatchThread(String text) {
            SwingUtilities.invokeLater(() -> displayThreadUnsafe(text));
        }

        private void displayThreadUnsafe(String text) {
            LogType logType = getLogType(text);
            // Replace \r\n by \n and suppress \r in MPSat output (inplace updating of unfolding info)
            text = text.replaceAll("\r\n", "\n").replaceAll("\r", "");

            if ((logType == LogType.STDOUT) || (logType == LogType.STDERR)) {
                text = getTextWithoutPrefix(text);
            }
            if ((oldLogType != null) && (oldLogType != logType) && !targetHasNewLine) {
                text = '\n' + text;
            }
            oldLogType = logType;
            targetHasNewLine = text.endsWith("\n");

            int lineCount = text.length() - text.replace("\n", "").length();
            for (int i = 0; i < lineCount; i++) {
                lineLogType.add(logType);
            }

            int fromPos = target.getDocument().getLength();
            int toPos = fromPos + text.length();
            target.insert(text, fromPos);
            target.setCaretPosition(toPos);
            paintHighlights();
        }

        private LogType getLogType(String text) {
            if (LogUtils.isInfoText(text)) {
                return LogType.INFO;
            }
            if (LogUtils.isWarningText(text)) {
                return LogType.WARNING;
            }
            if (LogUtils.isErrorText(text)) {
                return LogType.ERROR;
            }
            if (LogUtils.isStdoutText(text)) {
                return LogType.STDOUT;
            }
            if (LogUtils.isStderrText(text)) {
                return LogType.STDERR;
            }
            if ("\n".equals(text)) {
                return oldLogType;
            }
            return null;
        }

        private void addLines(int testCount) {
            startTime = Instant.now();
            for (int testIndex = 0; testIndex < testCount; testIndex++) {
                LogUtils.logMessage("Iteration #" + testIndex);
                LogUtils.logInfo(TextUtils.repeat("info ", 100));
                LogUtils.logMessage(TextUtils.repeat("message ", 10));
                LogUtils.logWarning(TextUtils.repeat("warning ", 50));
                LogUtils.logMessage(TextUtils.repeat("message ", 10));
                LogUtils.logError(TextUtils.repeat("error ", 25));
                LogUtils.logMessage(TextUtils.repeat("message ", 10));
                LogUtils.logStderr(TextUtils.repeat("stderr ", 25));
                LogUtils.logMessage(TextUtils.repeat("message ", 10));
                LogUtils.logStdout(TextUtils.repeat("stdout ", 25));
            }
            LogUtils.logMessage("DONE");
        }

        private void printDuration() {
            Instant finishTime = Instant.now();
            Duration duration = Duration.between(startTime, finishTime);
            LogUtils.logMessage("DURATION: " + duration.getSeconds() + '.' +  duration.getNano() + "sec");
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

    public OutputLogPanel(Runnable updater) {
        super();
        registerContentChangeListener(() -> {
            if (getTextEditor().isEmpty()) {
                lineLogType.clear();
            }
            if (updater != null) {
                updater.run();
            }
        });
    }

    public void captureStream() {
        if (!streamCaptured) {
            OutputStreamView outView = new OutputStreamView(new ByteArrayOutputStream(), getTextEditor());
            PrintStream outStream = new PrintStream(outView, true, StandardCharsets.UTF_8);
            systemOut = System.out;
            registerViewportChangeListener(this::paintHighlights);
            System.setOut(outStream);
            streamCaptured = true;
        }
    }

    public void releaseStream() {
        if (streamCaptured) {
            registerViewportChangeListener(null);
            System.setOut(systemOut);
            systemOut = null;
            streamCaptured = false;
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        TextEditor textEditor = getTextEditor();
        if (textEditor != null) {
            textEditor.setForeground(LogCommonSettings.getTextColor());
            textEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize()));
            paintHighlights();
        }
    }

    private void paintHighlights() {
        TextEditor textEditor = getTextEditor();
        textEditor.getHighlighter().removeAllHighlights();
        Pair<Integer, Integer> visibleRange = getVisibleRange();
        int startVisiblePos = visibleRange.getFirst();
        int endVisiblePos = visibleRange.getSecond();
        try {
            int startVisibleLineIndex = textEditor.getLineOfOffset(startVisiblePos);
            int endVisibleLineIndex = textEditor.getLineOfOffset(endVisiblePos);
            for (int visibleLineIndex = startVisibleLineIndex; visibleLineIndex <= endVisibleLineIndex; visibleLineIndex++) {
                LogType logType = (visibleLineIndex < lineLogType.size()) ? lineLogType.get(visibleLineIndex) : null;
                if (logType != null) {
                    Color highlightColor = getHighlightColor(logType);
                    HighlightUtils.highlightLine(textEditor, visibleLineIndex, highlightColor);
                }
            }
        } catch (BadLocationException ignored) {
        }
    }

    private Color getHighlightColor(LogType logType) {
        return (logType == null) ? null : switch (logType) {
            case INFO -> LogCommonSettings.getInfoBackground();
            case WARNING -> LogCommonSettings.getWarningBackground();
            case ERROR -> LogCommonSettings.getErrorBackground();
            case STDOUT -> LogCommonSettings.getStdoutBackground();
            case STDERR -> LogCommonSettings.getStderrBackground();
        };
    }

}
