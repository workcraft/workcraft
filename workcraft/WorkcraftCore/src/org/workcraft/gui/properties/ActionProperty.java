package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ActionProperty implements PropertyClass<Action, Action> {

    @Override
    public TableCellEditor getCellEditor() {
        return new ActionCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new ActionCellRenderer();
    }

    @Override
    public Action fromCellEditorValue(Action value) {
        return value;
    }

    @Override
    public Action toCellRendererValue(Action value) {
        return value;
    }

}
