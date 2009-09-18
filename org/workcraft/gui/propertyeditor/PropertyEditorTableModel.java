package org.workcraft.gui.propertyeditor;

import java.util.Map;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class PropertyEditorTableModel extends AbstractTableModel {
	static final String [] columnNames = { "property", "value" };
	PropertyDescriptor[] declarations = null;
	PropertyClass[] rowClasses = null;
	PropertyEditable object = null;

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	public void setObject(PropertyEditable object) {
		if (object == null) {
			clearObject();
			return;
		}

		this.object = object;
		declarations =  object.getPropertyDeclarations().toArray(new PropertyDescriptor[0]);
		rowClasses = new PropertyClass[declarations.length];

		fireTableDataChanged();
		fireTableStructureChanged();
	}

	public void clearObject() {
		object = null;
		declarations = null;
		rowClasses = null;

		fireTableDataChanged();
		fireTableStructureChanged();
	}

	public void setRowClass(int i, PropertyClass cls) {
		rowClasses[i] = cls;
	}

	public int getColumnCount() {
		if (object == null)
			return 0;
		else
			return 2;
	}

	public int getRowCount() {
		if (object == null)
			return 0;
		else
			return declarations.length;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (col == 0)
			return false;
		else
			return (declarations[row].isWritable());
	}

	public PropertyDescriptor getRowDeclaration(int i) {
		return declarations[i];
	}

	public Object getValueAt(int row, int col) {
		if (col ==0 )
			return declarations[row].getName();
		else try {
			Object value = declarations[row].getValue(object);
			if (rowClasses[row] != null)
				return rowClasses[row].toCellRendererValue(value);
			else
			{
				Map<Object, String> choice = declarations[row].getChoice();
				if(choice != null)
					return choice.get(value);
				else
					return value.toString();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			return "#EXCEPTION";
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		try {
			PropertyDescriptor desc = declarations[row];

			if (rowClasses[row] != null)
				value = rowClasses[row].fromCellEditorValue(value);

			desc.setValue(object, value);

			object.firePropertyChanged(desc.getName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public PropertyEditable getObject() {
		return object;
	}
}