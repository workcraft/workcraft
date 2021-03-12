package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.Map;

public abstract class AbstractBasicProperty<V> implements PropertyClass<V, String> {

    @Override
    public TableCellRenderer getCellRenderer() {
        return new BasicCellRenderer();
    }

    @Override
    public TableCellEditor getCellEditor() {
        return new BasicCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer(boolean hasPredefinedValues) {
        return new BasicCellRenderer(hasPredefinedValues);
    }

    @Override
    public TableCellEditor getCellEditor(Map<V, String> predefinedValues) {
        return new BasicCellEditor(predefinedValues);
    }

}
