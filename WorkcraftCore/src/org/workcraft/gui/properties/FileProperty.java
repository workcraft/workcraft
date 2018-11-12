package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.io.File;

public class FileProperty implements PropertyClass<File, File> {

    @Override
    public TableCellEditor getCellEditor() {
        return new FileCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new FileCellRenderer();
    }

    @Override
    public File fromCellEditorValue(File value) {
        return value;
    }

    @Override
    public File toCellRendererValue(File value) {
        return value;
    }

}
