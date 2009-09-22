/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui.propertyeditor;

import java.awt.Color;
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
		setFocusable(false);

		propertyClasses = new HashMap<Class<?>, PropertyClass>();
		propertyClasses.put(int.class, new IntegerProperty());
		propertyClasses.put(double.class, new DoubleProperty());
		propertyClasses.put(String.class, new StringProperty());
		propertyClasses.put(boolean.class, new BooleanProperty());
		propertyClasses.put(Color.class, new ColorProperty());
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
			PropertyDescriptor decl = model.getRowDeclaration(i);

			// If object declares a predefined set of values, use a ComboBox to edit the property regardless of class
			if (decl.getChoice() != null) {
				model.setRowClass(i, null);

				cellRenderers[i] = new DefaultTableCellRenderer();
				ChoiceCellEditor ce = new ChoiceCellEditor(decl);
				cellEditors[i] = ce;
			} else {
				// otherwise, try to get a corresponding PropertyClass object, that knows how to edit a property of this class
				PropertyClass cls = propertyClasses.get(decl.getType());
				model.setRowClass(i, cls);

				if (cls == null) {
					// no PropertyClass exists for this class, fall back to read-only mode using Object.toString()

					System.err
					.println("Data class \""
							+ decl.getType().getName()
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