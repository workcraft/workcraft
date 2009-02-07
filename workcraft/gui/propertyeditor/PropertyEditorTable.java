package org.workcraft.gui.propertyeditor;

import java.util.HashMap;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class PropertyEditorTable extends JTable implements PropertyEditor {
	HashMap<Class<?>, PropertyClass> propertyClasses;
	TableCellRenderer cellRenderers[];
	TableCellEditor cellEditors[];

	PropertyEditorTableModel model;

	public PropertyEditorTable() {
		super();

		model = new PropertyEditorTableModel();
		setModel(model);

		setTableHeader(null);


		propertyClasses = new HashMap<Class<?>, PropertyClass>();
		propertyClasses.put(int.class, new IntegerProperty());
		propertyClasses.put(double.class, new DoubleProperty());
	}

	public void setRowClass(int row, Class<?> cls, String[] data) {
		/*
		 * if (cls == String.class || cls == Integer.class || cls ==
		 * Double.class) { JTextField editor = new JTextField();
		 * rowEditors.put(new Integer(row), new DefaultCellEditor(editor));
		 * rowRenderers.put(new Integer(row), new DefaultTableCellRenderer()); }
		 * else if (cls == Boolean.class) { JComboBox editor = new
		 * JComboBox(boolValues); //JCheckBox editor = new JCheckBox();
		 * rowEditors.put(new Integer(row), new DefaultCellEditor(editor));
		 * rowRenderers.put(new Integer(row), new DefaultTableCellRenderer()); }
		 * else if (cls == Colorf.class) { rowEditors.put(new Integer(row), new
		 * ColorCellEditor()); rowRenderers.put(new Integer(row), new
		 * ColorCellRenderer(true)); } else if (cls == EnumWrapper.class) {
		 *
		 * EnumWrapper w[] = new EnumWrapper[data.length]; for (int i=0;
		 * i<data.length; i++) w[i] = new EnumWrapper (i, data[i]); JComboBox
		 * editor = new JComboBox(w); rowEditors.put(new Integer(row), new
		 * DefaultCellEditor(editor)); rowRenderers.put(new Integer(row), new
		 * DefaultTableCellRenderer()); } else if (cls == File.class) {
		 * rowEditors.put(new Integer(row), new FileCellEditor());
		 * rowRenderers.put(new Integer(row), new DefaultTableCellRenderer()); }
		 * else if (cls == Object.class) { JComboBox editor = new
		 * JComboBox(data); rowEditors.put(new Integer(row), new
		 * DefaultCellEditor(editor)); rowRenderers.put(new Integer(row), new
		 * DefaultTableCellRenderer()); } else rowEditors.put(new Integer(row),
		 * null);
		 */
	}


	@Override
	public TableCellEditor getCellEditor(int row, int col) {
		if (col == 0)
			return super.getCellEditor(row, col);
		else
			return cellEditors[row];
	}


	@Override
	public TableCellRenderer getCellRenderer(int row, int col) {
		if (col == 0)
			return super.getCellRenderer(row, col);
		else
			return cellRenderers[row];
	}

	public void clearObject() {
		model.clearObject();
	}

	public void setObject(PropertyEditable o) {
		model.setObject(o);

		cellRenderers = new TableCellRenderer[model.getRowCount()];
		cellEditors = new TableCellEditor[model.getRowCount()];


		for (int i = 0; i < model.getRowCount(); i++) {
			PropertyDeclaration decl = model.getRowDeclaration(i);


			// If object declares a predefined set of values, use a ComboBox to edit the property regardless of class
			if (decl.predefinedValues != null) {


			} else {
				// otherwise, try to get a corresponding PropertyClass object, that knows how to edit a property of this class
				PropertyClass cls = propertyClasses.get(decl.cls);
				model.setRowClass(i, cls);

				if (cls == null) {
					// no PropertyClass exists for this class, fall back to read-only mode using Object.toString()

					System.err
					.println("Data class \""
							+ decl.cls.getName()
							+ "\" is not supported by the Property Editor.");

					cellRenderers[i] = new DefaultTableCellRenderer();
					cellEditors[i] = null;
				} else {
					cellRenderers[i] = cls.getCellRenderer();
					cellEditors[i] = cls.getCellEditor();
				}


			}

		}
	}

	public PropertyEditable getObject() {
		return model.getObject();
	}

}