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
        return new BasicCellEditor(null, this);
    }

    @Override
    public TableCellRenderer getCellRenderer(boolean hasPredefinedValues) {
        return new BasicCellRenderer(hasPredefinedValues);
    }

    @Override
    public TableCellEditor getCellEditor(Map<V, String> predefinedValues) {
        return new BasicCellEditor(predefinedValues, this);
    }

    /**
     * Check whether the text being typed can still be completed to a valid value, so that the editor marks only
     * the text that cannot (e.g. "-" or "1e" are valid beginnings of a number, while "9o" is not).
     */
    public boolean isValidBeginning(String text) {
        return true;
    }

}
