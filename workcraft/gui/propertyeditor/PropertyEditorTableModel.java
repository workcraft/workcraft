package org.workcraft.gui.propertyeditor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class PropertyEditorTableModel extends AbstractTableModel {
	static final String [] columnNames = { "property", "value" };
	PropertyDeclaration[] declarations = null;
	PropertyClass[] rowClasses = null;
	PropertyEditable object = null;

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	public void setObject(PropertyEditable object) {
		this.object = object;
		declarations =  object.getPropertyDeclarations().toArray(new PropertyDeclaration[0]);
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
			return (declarations[row].setter != null);
	}

	public PropertyDeclaration getRowDeclaration(int i) {
		return declarations[i];
	}

	public Object getValueAt(int row, int col) {
		if (col ==0 )
			return declarations[row].name;
		else try {
			Method m = object.getClass().getMethod(declarations[row].getter,  (Class[])null);
			if (rowClasses[row] != null)
				return rowClasses[row].toCellRendererValue(m.invoke(object, (Object[])null));
			else
				return declarations[row].valueNames.get(m.invoke(object, (Object[])null));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return "#NO SUCH METHOD";
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return "#EXCEPTION";
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return "#UNACCESIBLE";
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		try {
			Method m = object.getClass().getMethod(declarations[row].setter,  declarations[row].cls );
			if (rowClasses[row] != null)
				m.invoke(object, rowClasses[row].fromCellEditorValue(value));
			else {
				if (!declarations[row].cls.isPrimitive())
					m.invoke(object, declarations[row].cls.cast(value));
				else {
					if (declarations[row].cls.equals(double.class)) {
						Double boxedValue = (Double)value;
						double unboxedValue = boxedValue.doubleValue();
						m.invoke(object, unboxedValue);
					} else if (declarations[row].cls.equals(int.class)) {
						Integer boxedValue = (Integer)value;
						int unboxedValue = boxedValue.intValue();
						m.invoke(object, unboxedValue);
					} else throw new RuntimeException ("");
				}
			}
			object.firePropertyChanged(declarations[row].name);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public PropertyEditable getObject() {
		return object;
	}
}