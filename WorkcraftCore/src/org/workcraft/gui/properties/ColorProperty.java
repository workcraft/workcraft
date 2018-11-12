package org.workcraft.gui.properties;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ColorProperty implements PropertyClass<Color, Color> {

    @Override
    public TableCellEditor getCellEditor() {
        return new ColorCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer() {
        return new ColorCellRenderer();
    }

    @Override
    public Color fromCellEditorValue(Color value) {
        return value;
    }

    @Override
    public Color toCellRendererValue(Color value) {
        return value;
    }

}
