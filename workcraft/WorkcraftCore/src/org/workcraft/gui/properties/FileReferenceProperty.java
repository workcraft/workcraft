package org.workcraft.gui.properties;

import org.workcraft.dom.references.FileReference;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class FileReferenceProperty implements PropertyClass<FileReference, FileReference> {

    @Override
    public TableCellEditor getCellEditor() {
        return new FileReferenceCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new FileReferenceCellRenderer();
    }

    @Override
    public FileReference fromCellEditorValue(FileReference value) {
        return value;
    }

    @Override
    public FileReference toCellRendererValue(FileReference value) {
        return value;
    }

}
