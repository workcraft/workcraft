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

package org.workcraft.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;

@SuppressWarnings("serial")
public class PropertyEditorWindow extends JPanel {
	private PropertyEditorTable propertyTable;
	private JScrollPane scrollProperties;

	public PropertyEditorWindow () {
		propertyTable = new PropertyEditorTable();

		scrollProperties = new JScrollPane();
		scrollProperties.setViewportView(propertyTable);

		setLayout(new BorderLayout(0,0));
		add(new DisabledPanel(), BorderLayout.CENTER);
		validate();

	}

	public Properties getObject () {
		return propertyTable.getObject();
	}

	public void setObject (Properties o) {
		removeAll();
		propertyTable.setObject(o);
		add(scrollProperties, BorderLayout.CENTER);
		validate();
		repaint();
	}

	public void clearObject () {
		if (propertyTable.getObject() != null) {
			removeAll();
			propertyTable.clearObject();
			add(new DisabledPanel(), BorderLayout.CENTER);
			validate();
			repaint();
		}

	}
}
