package org.workcraft.gui;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.controls.LogPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ErrorWindow extends LogPanel implements ComponentListener {

    class ErrorStreamView extends FilterOutputStream implements ChangeListener {
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
                highlightTab();
            });
        }

        private void highlightTab() {
            Container component = getParent().getParent();
            Container parent = component.getParent();
            if (parent instanceof JTabbedPane tabbedPane) {
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    if (tabbedPane.getComponentAt(i) != component) continue;

                    Component tabComponent = tabbedPane.getTabComponentAt(i);
                    if (Color.RED.equals(tabComponent.getForeground())) continue;

                    colorBack = tabComponent.getForeground();
                    tabComponent.setForeground(Color.RED);
                    tabbedPane.removeChangeListener(this);
                    tabbedPane.addChangeListener(this);
                }
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (colorBack == null) {
                return;
            }
            Container component = getParent().getParent();
            Container parent = component.getParent();
            if (parent instanceof JTabbedPane tab) {
                if (tab.getSelectedComponent() == component) {
                    int selectedIndex = tab.getSelectedIndex();
                    Component tabComponent = tab.getTabComponentAt(selectedIndex);
                    tabComponent.setForeground(colorBack);
                    colorBack = null;
                    tab.removeChangeListener(this);
                }
            }
        }
    }

    private PrintStream systemErr;
    private boolean streamCaptured = false;
    private Color colorBack = null;

    public ErrorWindow() {
        getTextArea().setForeground(Color.RED);
        addComponentListener(this);
    }

    public void captureStream() {
        if (!streamCaptured) {
            ErrorStreamView streamView = new ErrorStreamView(new ByteArrayOutputStream(), getTextArea());
            PrintStream errPrintStream = new PrintStream(streamView);
            systemErr = System.err;
            System.setErr(errPrintStream);
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

    @Override
    public void componentShown(ComponentEvent e) {
        if (colorBack == null) {
            return;
        }

        Container component = getParent().getParent();
        Container parent = getParent();
        if (parent instanceof JTabbedPane tab) {
            for (int i = 0; i < tab.getComponentCount(); i++) {
                if (tab.getComponentAt(i) == component) {
                    tab.setForegroundAt(i, colorBack);
                    colorBack = null;
                }
            }
        }
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentResized(ComponentEvent e) {
    }

}
