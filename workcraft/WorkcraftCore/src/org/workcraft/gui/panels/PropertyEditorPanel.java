package org.workcraft.gui.panels;

import org.workcraft.gui.properties.Properties;
import org.workcraft.gui.properties.PropertyEditorTable;

import javax.swing.*;
import java.awt.*;

public class PropertyEditorPanel extends JPanel {

    private final DisabledPanel disabledPanel = new DisabledPanel();
    private final PropertyEditorTable propertyTable = new PropertyEditorTable();
    private final JScrollPane scrollPane = new JScrollPane();
    private boolean empty;

    // Properties (and the component below them) to set once the current edit is finished
    private Properties pendingProperties = null;
    private JComponent pendingBottomComponent = null;

    public PropertyEditorPanel() {
        setLayout(new BorderLayout());
        scrollPane.setViewportView(propertyTable);
        add(disabledPanel, BorderLayout.CENTER);
        propertyTable.setEditingFinishedHandler(this::applyPending);
        empty = true;
    }

    public void set(Properties properties) {
        set(properties, null);
    }

    public void set(Properties properties, JComponent bottomComponent) {
        if (propertyTable.isEditing()) {
            // Refresh requested by a previous edit must not close the editor of another cell that was activated
            // in the meantime (it would take two clicks), so it is postponed until the edit is finished.
            pendingProperties = properties;
            pendingBottomComponent = bottomComponent;
            empty = properties.getDescriptors().isEmpty();
            return;
        }
        pendingProperties = null;
        pendingBottomComponent = null;
        removeAll();
        propertyTable.assign(properties);
        add(scrollPane, BorderLayout.CENTER);
        if (bottomComponent != null) {
            add(bottomComponent, BorderLayout.SOUTH);
        }
        validate();
        repaint();
        if (bottomComponent != null) {
            // A hack to display the component: toggle its visibility a couple of times.
            bottomComponent.setVisible(false);
            bottomComponent.setVisible(true);
        }
        empty = properties.getDescriptors().isEmpty();
    }

    private void applyPending() {
        if (pendingProperties != null) {
            set(pendingProperties, pendingBottomComponent);
        }
    }

    public void clear() {
        pendingProperties = null;
        pendingBottomComponent = null;
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
