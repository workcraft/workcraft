package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class LegendListProperty implements PropertyClass<LegendList, LegendList> {

    @Override
    public TableCellEditor getCellEditor() {
        return new LegendListCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new LegendListCellRenderer();
    }

    @Override
    public LegendList fromCellEditorValue(LegendList value) {
        return value;
    }

    @Override
    public LegendList toCellRendererValue(LegendList value) {
        return value;
    }

}
