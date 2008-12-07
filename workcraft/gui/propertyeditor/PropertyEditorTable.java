package org.workcraft.gui.propertyeditor;

import java.util.HashMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
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

		this.model = new PropertyEditorTableModel();
		setModel(this.model);

		this.propertyClasses = new HashMap<Class<?>, PropertyClass>();
		this.propertyClasses.put(Integer.class, new IntegerProperty());
		this.propertyClasses.put(Double.class, new DoubleProperty());
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


	public TableCellEditor getCellEditor(int row, int col) {
		if (col == 0)
			return super.getCellEditor(row, col);
		else
			return this.cellEditors[row];
	}


	public TableCellRenderer getCellRenderer(int row, int col) {
		if (col == 0)
			return super.getCellRenderer(row, col);
		else
			return this.cellRenderers[row];
	}

	public void clearObject() {
		this.model.clearObject();
	}

	public void setObject(PropertyEditable o) {
		this.model.setObject(o);

		this.cellRenderers = new TableCellRenderer[this.model.getRowCount()];
		this.cellEditors = new TableCellEditor[this.model.getRowCount()];

		for (int i = 0; i < this.model.getRowCount(); i++) {
			PropertyClass cls = this.propertyClasses.get(this.model
					.getRowClass(i));

			if (cls == null) {
				System.err
						.println("Data class \""
								+ this.model.getRowClass(i).getName()
								+ "\" is not supported by the Property Editor. Default cell editor will be used, which may or may not work.");
				this.cellRenderers[i] = new DefaultTableCellRenderer();
				this.cellEditors[i] = new DefaultCellEditor(new JTextField());
			} else {
				this.cellRenderers[i] = cls.getCellRenderer();
				this.cellEditors[i] = cls.getCellEditor();

			}
		}
	}

}