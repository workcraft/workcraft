package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class TextActionProperty implements PropertyClass<TextAction, TextAction> {

    @Override
    public TableCellEditor getCellEditor() {
        return new TextActionCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new TextActionCellRenderer();
    }

    @Override
    public TextAction fromCellEditorValue(TextAction value) {
        return value;
    }

    @Override
    public TextAction toCellRendererValue(TextAction value) {
        return value;
    }

}
