package org.workcraft.gui.tools;

import org.workcraft.dom.visual.SizeHelper;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import java.util.ArrayList;
import java.util.List;

public class BasicTable<T> extends JTable {

    private final List<T> items = new ArrayList<>();

    public BasicTable(String title) {
        setModel(new BasicTableModel<>(title, items));
        setFocusable(false);
        setRowSelectionAllowed(false);
        setRowHeight(SizeHelper.getComponentHeightFromFont(getFont()));
        setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        if (title == null) {
            setTableHeader(null);
        }
    }

    public void clear() {
        items.clear();
        refresh();
    }

    public void set(List<T> values) {
        items.clear();
        items.addAll(values);
        refresh();
    }

    private void refresh() {
        tableChanged(new TableModelEvent(getModel()));
    }

}
