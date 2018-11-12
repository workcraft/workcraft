package org.workcraft.plugins.cpog.properties;

import org.workcraft.gui.properties.PropertyClass;
import org.workcraft.plugins.cpog.Encoding;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class EncodingProperty implements PropertyClass<Encoding, Encoding> {

    @Override
    public TableCellEditor getCellEditor() {
        return new EncodingCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new DefaultTableCellRenderer();
    }

    @Override
    public Encoding fromCellEditorValue(Encoding editorComponentValue) {
        return editorComponentValue;
    }

    @Override
    public Encoding toCellRendererValue(Encoding value) {
        return value;
    }

}
