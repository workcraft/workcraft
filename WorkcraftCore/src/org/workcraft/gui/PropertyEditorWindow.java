package org.workcraft.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;

@SuppressWarnings("serial")
public class PropertyEditorWindow extends JPanel {

    private final DisabledPanel disabledPanel;
    private final PropertyEditorTable propertyTable;
    private final JScrollPane scrollPane;

    public PropertyEditorWindow() {
        setLayout(new BorderLayout());
        disabledPanel = new DisabledPanel();
        propertyTable = new PropertyEditorTable();
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(propertyTable);
        add(disabledPanel, BorderLayout.CENTER);
        validate();
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
    }

    public void clearObject() {
        if (propertyTable.getObject() != null) {
            removeAll();
            propertyTable.clearObject();
            add(disabledPanel, BorderLayout.CENTER);
            validate();
            repaint();
        }
    }

}
