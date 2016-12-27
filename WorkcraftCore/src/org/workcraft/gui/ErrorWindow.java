package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;

import org.workcraft.dom.visual.SizeHelper;

@SuppressWarnings("serial")
public class ErrorWindow extends JPanel implements ComponentListener {
    protected PrintStream systemErr;
    protected boolean streamCaptured = false;
    private final JTextArea txtStdErr;
    private Color colorBack = null;

    public ErrorWindow() {
        txtStdErr = new JTextArea();
        txtStdErr.setLineWrap(true);
        txtStdErr.setEditable(false);
        txtStdErr.setWrapStyleWord(true);
        txtStdErr.setForeground(Color.RED);
        txtStdErr.addMouseListener(new LogAreaMouseListener());

        DefaultCaret caret = (DefaultCaret) txtStdErr.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollStdErr = new JScrollPane();
        scrollStdErr.setViewportView(txtStdErr);

        setLayout(new BorderLayout(0, 0));
        this.add(scrollStdErr, BorderLayout.CENTER);

        addComponentListener(this);
    }

    class ErrorStreamView extends FilterOutputStream implements ChangeListener {
        JTextArea target;

        ErrorStreamView(OutputStream aStream, JTextArea target) {
            super(aStream);
            this.target = target;
        }

        public void puts(String s) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Container parent = getParent().getParent().getParent();
                    if (parent instanceof JTabbedPane) {
                        JTabbedPane tab = (JTabbedPane) parent;
                        for (int i = 0; i < tab.getTabCount(); i++) {
                            if (tab.getComponentAt(i) == getParent().getParent()) {
                                Component tabComponent = tab.getTabComponentAt(i);
                                if (!tabComponent.getForeground().equals(Color.RED)) {
                                    colorBack = tabComponent.getForeground();
                                    tabComponent.setForeground(Color.RED);
                                    tab.removeChangeListener(ErrorStreamView.this);
                                    tab.addChangeListener(ErrorStreamView.this);
                                }
                            }
                        }
                    }
                }
            });
            target.append(s);
            target.setFont(new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize()));
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

        public void stateChanged(ChangeEvent e) {
            if (colorBack == null) {
                return;
            }
            Container parent = getParent().getParent().getParent();
            if (parent instanceof JTabbedPane) {
                JTabbedPane tab = (JTabbedPane) parent;
                if (tab.getSelectedComponent() == getParent().getParent()) {
                    tab.getTabComponentAt(tab.getSelectedIndex()).setForeground(colorBack);
                    colorBack = null;
                    tab.removeChangeListener(this);
                }
            }
        }
    }

    public void captureStream() {
        if (!streamCaptured) {
            PrintStream errPrintStream = new PrintStream(new ErrorStreamView(
                    new ByteArrayOutputStream(), txtStdErr));

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

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
        if (colorBack == null) {
            return;
        }

        Container parent = getParent().getParent().getParent();
        if (parent instanceof JTabbedPane) {
            JTabbedPane tab = (JTabbedPane) parent;
            for (int i = 0; i < tab.getComponentCount(); i++) {
                if (tab.getComponentAt(i) == this.getParent().getParent()) {
                    tab.setForegroundAt(i, colorBack);
                    colorBack = null;
                }
            }
        }
    }
}
