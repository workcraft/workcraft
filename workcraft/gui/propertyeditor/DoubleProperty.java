package org.workcraft.gui.propertyeditor;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class DoubleProperty implements PropertyClass {
	JTextField edit = new JTextField();

	public TableCellEditor getCellEditor() {
		DefaultCellEditor dce = new DefaultCellEditor(edit);
		dce.setClickCountToStart(0);
		return dce;

		//		return new TableCellEditor() {
		//
		//
		//
		//			public Component getTableCellEditorComponent(JTable table,
		//					Object value, boolean isSelected, int row, int column) {
		//				edit.setText(value.toString());
		//				if (!isSelected) {
		//					edit.setBorder(null);
		//					edit.setBackground(table.getBackground());
		//					edit.setForeground(table.getForeground());
		//
		//				} else {
		//					edit.setBackground(table.getSelectionBackground());
		//					edit.setForeground(table.getSelectionForeground());
		//				}
		//				return edit;
		//			}
		//
		//
		//			public void addCellEditorListener(CellEditorListener l) {
		//			}
		//
		//
		//			public void cancelCellEditing() {
		//
		//			}
		//
		//
		//			public Object getCellEditorValue() {
		//				return edit.getText();
		//			}
		//
		//
		//			public boolean isCellEditable(EventObject anEvent) {
		//			    if (anEvent instanceof MouseEvent)
		//					return ((MouseEvent)anEvent).getClickCount() >= 1;
		//					else
		//						return false;
		//
		//
		//			}
		//
		//
		//			public void removeCellEditorListener(CellEditorListener l) {
		//			}
		//
		//
		//			public boolean shouldSelectCell(EventObject anEvent) {
		//
		//				return true;
		//			}
		//
		//
		//			public boolean stopCellEditing() {
		//				return true;
		//			}
		//		};
	}


	public TableCellRenderer getCellRenderer() {
		return new DefaultTableCellRenderer();

	}


	public Object fromComponentValue(Object editorComponentValue) {
		return Double.parseDouble((String)editorComponentValue);
	}


	public Object toComponentValue(Object value) {
		return value.toString();
	}

}
