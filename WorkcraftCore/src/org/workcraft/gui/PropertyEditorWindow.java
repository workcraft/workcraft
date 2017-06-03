package org.workcraft.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;

@SuppressWarnings("serial")
public class PropertyEditorWindow extends JPanel {

    private final DisabledPanel disabledPanel = new DisabledPanel();
    private final PropertyEditorTable propertyTable = new PropertyEditorTable();
    private final JScrollPane scrollPane = new JScrollPane();
    private boolean empty;

    public PropertyEditorWindow() {
        setLayout(new BorderLayout());
        scrollPane.setViewportView(propertyTable);
        add(disabledPanel, BorderLayout.CENTER);
        empty = true;
    }

    public Properties getObject() {
        return propertyTable.getObject();
    }

    public void setObject(Properties o) {
        removeAll();
        propertyTable.setObject(o);
        add(scrollPane, BorderLayout.CENTER);
        validate();
        repaint();
        empty = o.getDescriptors().size() == 0;
    }

    public void clearObject() {
        if (propertyTable.getObject() != null) {
            removeAll();
            propertyTable.clearObject();
            add(disabledPanel, BorderLayout.CENTER);
            validate();
            repaint();
        }
        empty = true;
    }

    public boolean isEmpty() {
        return empty;
    }

}
