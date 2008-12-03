package org.workcraft.gui.propertyeditor;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.workcraft.framework.plugins.Plugin;

public interface PropertyClass {
	public TableCellRenderer getCellRenderer();
	public TableCellEditor getCellEditor();
}
