package org.workcraft.gui.panels;

import org.workcraft.dom.visual.SizeHelper;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ErrorLogPanel extends LogPanel {

    class ErrorStreamView extends FilterOutputStream {
        private final JTextArea target;

        ErrorStreamView(OutputStream aStream, JTextArea target) {
            super(aStream);
            this.target = target;
        }

        @Override
        public void write(byte[] b) throws IOException {
            if (systemErr != null) {
                systemErr.write(b);
            }
            displayInEventDispatchThread(new String(b, StandardCharsets.UTF_8));
        }

        @Override
        public void write(byte[] b, int off, int len) {
            if (systemErr != null) {
                systemErr.write(b, off, len);
            }
            displayInEventDispatchThread(new String(b, off, len, StandardCharsets.UTF_8));
        }

        private void displayInEventDispatchThread(String s) {
            SwingUtilities.invokeLater(() -> {
                target.append(s);
                target.setFont(new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize()));
            });
        }
    }

    private PrintStream systemErr;
    private boolean streamCaptured = false;

    public ErrorLogPanel(Runnable updater) {
        super();
        registerContentChangeListener(updater);
        getTextEditor().setForeground(Color.RED);
    }

    public void captureStream() {
        if (!streamCaptured) {
            ErrorStreamView errView = new ErrorStreamView(new ByteArrayOutputStream(), getTextEditor());
            PrintStream errStream = new PrintStream(errView);
            systemErr = System.err;
            System.setErr(errStream);
            streamCaptured = true;
        }
    }

    public void releaseStream() {
        if (streamCaptured) {
            System.setErr(systemErr);
            systemErr = null;
            streamCaptured = false;
        }
    }

}
