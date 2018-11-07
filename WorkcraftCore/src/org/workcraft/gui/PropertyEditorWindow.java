package org.workcraft.gui;

import org.workcraft.gui.properties.Properties;
import org.workcraft.gui.properties.PropertyEditorTable;

import javax.swing.*;
import java.awt.*;

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

    public void set(Properties properties) {
        removeAll();
        propertyTable.assign(properties);
        add(scrollPane, BorderLayout.CENTER);
        validate();
        repaint();
        empty = properties.getDescriptors().size() == 0;
    }

    public void clear() {
        removeAll();
        propertyTable.clear();
        add(disabledPanel, BorderLayout.CENTER);
        validate();
        repaint();
        empty = true;
    }

    public boolean isEmpty() {
        return empty;
    }

}
