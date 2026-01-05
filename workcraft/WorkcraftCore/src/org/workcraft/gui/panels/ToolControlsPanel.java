package org.workcraft.gui.panels;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ToolControlsWindow extends JPanel {

    private final JPanel disabledPanel = new DisabledPanel();
    private final JScrollPane scrollPane  = new JScrollPane();
    private boolean empty = true;

    public ToolControlsWindow() {
        setLayout(new BorderLayout());
        add(disabledPanel, BorderLayout.CENTER);
    }

    public void setContent(JPanel panel) {
        removeAll();
        if (panel == null) {
            add(disabledPanel, BorderLayout.CENTER);
            empty = true;
        } else {
            add(scrollPane, BorderLayout.CENTER);
            scrollPane.setViewportView(panel);
            empty = panel.getComponentCount() == 0;
        }
    }

    public boolean isEmpty() {
        return empty;
    }

}
