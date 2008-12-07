package org.workcraft.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.workcraft.framework.Framework;
import org.workcraft.gui.propertyeditor.PropertyEditable;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;

@SuppressWarnings("serial")
public class PropertyView extends JPanel {
	private PropertyEditorTable propertyTable;
	private JScrollPane scrollProperties;

	public PropertyView (Framework framework) {
		this.propertyTable = new PropertyEditorTable();

		this.scrollProperties = new JScrollPane();
		this.scrollProperties.setViewportView(this.propertyTable);

		setLayout(new BorderLayout(0,0));
		this.add(this.scrollProperties, BorderLayout.CENTER);
	}

	public void setObject (PropertyEditable o) {
		this.propertyTable.setObject(o);

	}
}
