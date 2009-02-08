package org.workcraft.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.workcraft.framework.Framework;
import org.workcraft.gui.propertyeditor.PropertyEditable;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;

@SuppressWarnings("serial")
public class PropertyEditorWindow extends JPanel {
	private PropertyEditorTable propertyTable;
	private JScrollPane scrollProperties;

	public PropertyEditorWindow (Framework framework) {
		propertyTable = new PropertyEditorTable();

		scrollProperties = new JScrollPane();
		scrollProperties.setViewportView(propertyTable);

		setLayout(new BorderLayout(0,0));
		this.add(new DisabledPanel(), BorderLayout.CENTER);

	}

	public PropertyEditable getObject () {
		return propertyTable.getObject();
	}

	public void setObject (PropertyEditable o) {
		removeAll();
		propertyTable.setObject(o);
		this.add(scrollProperties, BorderLayout.CENTER);
		this.validate();
	}

	public void clearObject () {
		removeAll();
		propertyTable.clearObject();

		this.add(new DisabledPanel(), BorderLayout.CENTER);
		this.validate();
	}
}
