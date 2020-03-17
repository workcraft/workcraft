package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ActionListProperty implements PropertyClass<ActionList, ActionList> {

    @Override
    public TableCellEditor getCellEditor() {
        return new ActionListCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new ActionListCellRenderer();
    }

    @Override
    public ActionList fromCellEditorValue(ActionList value) {
        return value;
    }

    @Override
    public ActionList toCellRendererValue(ActionList value) {
        return value;
    }

}
