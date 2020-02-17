package org.workcraft.gui;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.PopupUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.*;

@SuppressWarnings("serial")
public class ErrorWindow extends JPanel implements ComponentListener {
    protected PrintStream systemErr;
    protected boolean streamCaptured = false;
    private final JTextArea txtStdErr;
    private Color colorBack = null;

    public ErrorWindow() {
        txtStdErr = new JTextArea();
        txtStdErr.setMargin(SizeHelper.getTextMargin());
        txtStdErr.setLineWrap(true);
        txtStdErr.setEditable(false);
        txtStdErr.setWrapStyleWord(true);
        txtStdErr.setForeground(Color.RED);
        PopupUtils.setTextAreaPopup(txtStdErr);

        DefaultCaret caret = (DefaultCaret) txtStdErr.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollStdErr = new JScrollPane();
        scrollStdErr.setViewportView(txtStdErr);

        setLayout(new BorderLayout());
        add(scrollStdErr, BorderLayout.CENTER);
        addComponentListener(this);
    }

    class ErrorStreamView extends FilterOutputStream implements ChangeListener {
        private final JTextArea target;

        ErrorStreamView(OutputStream aStream, JTextArea target) {
            super(aStream);
            this.target = target;
        }

        public void puts(String s) {
            SwingUtilities.invokeLater(() -> highlightTab());
            target.append(s);
            target.setFont(new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize()));
        }

        private void highlightTab() {
            Container component = getParent().getParent();
            Container parent = component.getParent();
            if (parent instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) parent;
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
        public void write(byte[] b) throws IOException {
            if (systemErr != null) {
                systemErr.write(b);
            }
            String s = new String(b);
            puts(s);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (systemErr != null) {
                systemErr.write(b, off, len);
            }
            String s = new String(b, off, len);
            puts(s);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (colorBack == null) {
                return;
            }
            Container component = getParent().getParent();
            Container parent = component.getParent();
            if (parent instanceof JTabbedPane) {
                JTabbedPane tab = (JTabbedPane) parent;
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

    public void captureStream() {
        if (!streamCaptured) {
            ErrorStreamView streamView = new ErrorStreamView(new ByteArrayOutputStream(), txtStdErr);
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
        if (parent instanceof JTabbedPane) {
            JTabbedPane tab = (JTabbedPane) parent;
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
