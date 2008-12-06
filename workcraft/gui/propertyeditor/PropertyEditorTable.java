package org.workcraft.gui.propertyeditor;
import java.util.HashMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
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

		model = new PropertyEditorTableModel();
		setModel(model);

		propertyClasses = new HashMap<Class<?>, PropertyClass>();
		propertyClasses.put(Integer.class, new IntegerProperty());
		propertyClasses.put(Double.class, new DoubleProperty());
	}

	public void setRowClass (int row, Class<?> cls, String[] data) {
/*		if (cls == String.class || cls == Integer.class || cls == Double.class) {
			JTextField editor = new JTextField();
			rowEditors.put(new Integer(row), new DefaultCellEditor(editor));
			rowRenderers.put(new Integer(row), new DefaultTableCellRenderer());
		} else if (cls == Boolean.class)
		{
			JComboBox editor = new JComboBox(boolValues);
			//JCheckBox editor = new JCheckBox();
			rowEditors.put(new Integer(row), new DefaultCellEditor(editor));
			rowRenderers.put(new Integer(row), new DefaultTableCellRenderer());
		} else if (cls == Colorf.class) {
			rowEditors.put(new Integer(row), new ColorCellEditor());
			rowRenderers.put(new Integer(row), new ColorCellRenderer(true));
		}	else if (cls == EnumWrapper.class) {

			EnumWrapper w[] = new EnumWrapper[data.length];
			for (int i=0; i<data.length; i++)
				w[i] = new EnumWrapper (i, data[i]);
			JComboBox editor = new JComboBox(w);
			rowEditors.put(new Integer(row), new DefaultCellEditor(editor));
			rowRenderers.put(new Integer(row), new DefaultTableCellRenderer());
		}	else if (cls == File.class) {
			rowEditors.put(new Integer(row), new FileCellEditor());
			rowRenderers.put(new Integer(row), new DefaultTableCellRenderer());
		}
			else if (cls == Object.class) {
			JComboBox editor = new JComboBox(data);
			rowEditors.put(new Integer(row), new DefaultCellEditor(editor));
			rowRenderers.put(new Integer(row), new DefaultTableCellRenderer());
		} else
			rowEditors.put(new Integer(row), null);*/
	}

	public TableCellEditor getCellEditor(int row, int col)
	{
		if (col == 0)
			return super.getCellEditor(row, col);
		else
			return cellEditors[row];
	}

	public TableCellRenderer getCellRenderer(int row, int col)
	{
		if (col == 0)
			return super.getCellRenderer(row, col);
		else
			return cellRenderers[row];
	}

	@Override
	public void clearObject() {
		model.clearObject();
	}

	@Override
	public void setObject(PropertyEditable o) {
		model.setObject(o);

		cellRenderers = new TableCellRenderer[model.getRowCount()];
		cellEditors = new TableCellEditor[model.getRowCount()];

		for (int i=0; i<model.getRowCount(); i++) {
			PropertyClass cls = propertyClasses.get(model.getRowClass(i));

			if (cls == null) {
				System.err.println ("Data class \""+model.getRowClass(i).getName()+"\" is not supported by the Property Editor. Default cell editor will be used, which may or may not work.");
				cellRenderers[i] = new DefaultTableCellRenderer();
				cellEditors[i] = new DefaultCellEditor(new JTextField());
			} else {
				cellRenderers[i] = cls.getCellRenderer();
				cellEditors[i] = cls.getCellEditor();

			}
		}
	}

}