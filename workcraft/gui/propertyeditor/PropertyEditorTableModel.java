package org.workcraft.gui.propertyeditor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class PropertyEditorTableModel extends AbstractTableModel {
	static final String [] columnNames = { "property", "value" };
	PropertyDeclaration[] declarations = null;


	Object object = null;

	/*	HashMap<String, Property> propertyMap = new HashMap<String, Property>();
	LinkedList<String> propertyNames = new LinkedList<String>();*/

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	public void setObject(PropertyEditable object) {
		this.object = object;
		declarations =  object.getPropertyDeclarations().toArray(new PropertyDeclaration[0]);


		/*

		// find all public methods starting with "get/is" or "set"

		Class<?> cls = object.getClass();
		while (cls != Object.class) {
			for (Method method : cls.getDeclaredMethods()) {

				if ( (method.getModifiers() & Modifier.PUBLIC) == 0)
					continue;


				String methodName = method.getName();

				boolean setMethod = methodName.startsWith("set");
				boolean getMethod = methodName.startsWith("get");
				boolean isMethod = methodName.startsWith("is");

				if ( setMethod || getMethod || isMethod) {
					if (method.getAnnotation(HiddenProperty.class) != null)
						continue;
					// property is public and not explicitly hidden

					String propertyName;

					PropertyName nameAnnotation = method.getAnnotation(PropertyName.class);
					if (nameAnnotation != null)
						propertyName = nameAnnotation.value();
					else {
						if (setMethod || getMethod)
							propertyName = methodName.substring(3);
						else
							propertyName = methodName.substring(2);
						propertyName = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
					}

					Property p = propertyMap.get(propertyName);
					if (p == null) {
						p = new Property();
						propertyMap.put(propertyName, p);
						propertyNames.add(propertyName);
					}

					if (setMethod)
						p.setter = methodName;
					else
						p.getter = methodName;
				}
			}
		cls = cls.getSuperclass();
	}

	Collections.sort(propertyNames);*/

		fireTableDataChanged();
	}

	public void clearObject() {
		object = null;
		declarations = null;
		fireTableDataChanged();
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

	public Class<?> getRowClass(int row) {
		if (object == null)
			return null;
		else
			return declarations[row].cls;
	}


	@Override
	public boolean isCellEditable(int row, int col) {
		if (col < 1)
			return false;
		else
			return (declarations[row].setter != null);
	}

	public Object getValueAt(int row, int col) {
		if (col==0)
			return declarations[row].name;
		else
			try {
				Method m = object.getClass().getMethod(declarations[row].getter,  (Class[])null );
				Object o = m.invoke(object, (Object[])null);
				return o;
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
		/*if (col==1) {
			Property p = propertyMap.get(propertyNames.get(row));
			}
			else {
				System.err.println ("PropertyEditorTableModel doesn't know how to use class " + propertyClasses[row].getName());
			}
			if (propertySetters[row]!=null) {
				try {
					Method m;
					if (propertyClasses[row] == EnumWrapper.class)
						m = object.getClass().getMethod(propertySetters[row],  Integer.class );
					else if (propertyClasses[row] == File.class)
						m = object.getClass().getMethod(propertySetters[row],  String.class );
						else
						m = object.getClass().getMethod(propertySetters[row],  propertyClasses[row] );
					m.invoke(object, brainyobject);
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}*/
	}
}
