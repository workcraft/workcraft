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

package org.workcraft.plugins.layout;

import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.Plugin;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class DotLayoutSettings implements SettingsPage {
	protected static boolean importConnectionsShape = false;
	protected static String dotCommand = "dot";

	private static LinkedList<PropertyDescriptor> properties;

	public DotLayoutSettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration(this, "Dot command", "getDotCommand", "setDotCommand", String.class));
		properties.add(new PropertyDeclaration(this, "Import connections shape from Dot graph (experimental)", "getImportConnectionsShape", "setImportConnectionsShape", Boolean.class));
	}
	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void load(Config config) {
		dotCommand = config.getString("DotLayout.dotCommand", "dot");
		importConnectionsShape = config.getBoolean("DotLayout.importConnectionsShape", false);
	}

	public void save(Config config) {
		config.set("DotLayout.dotCommand", dotCommand)	;
		config.setBoolean("DotLayout.importConnectionsShape", importConnectionsShape);
	}

	public static Boolean getImportConnectionsShape() {
		return importConnectionsShape;
	}

	public static void setImportConnectionsShape(Boolean value) {
		importConnectionsShape = value;
	}

	public static String getDotCommand() {
		return DotLayoutSettings.dotCommand;
	}

	public static void setDotCommand(String dotCommand) {
		DotLayoutSettings.dotCommand = dotCommand;
	}

	public String getSection() {
		return "Layout";
	}
	@Override
	public String getName() {
		return "Dot";
	}
}
