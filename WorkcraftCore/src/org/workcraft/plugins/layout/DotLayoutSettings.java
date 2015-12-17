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
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class DotLayoutSettings implements Settings {
	public static final String BUNDLED_DIRECTORY = "tools/dot/";
	private static final LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
	private static final String prefix = "DotLayout";

	private static final String keyImportConnectionsShape  = prefix + ".importConnectionsShape";
	private static final String keyDotCommand  = prefix + ".dotCommand";

	private static final String defaultDotCommand = "dot";
	private static final boolean defaultImportConnectionsShape = true;

	private static String dotCommand = defaultDotCommand;
	private static boolean importConnectionsShape = defaultImportConnectionsShape;

	public DotLayoutSettings() {
		properties.add(new PropertyDeclaration<DotLayoutSettings, String>(
				this, "Dot command", String.class, true, false, false) {
			protected void setter(DotLayoutSettings object, String value) {
				setCommand(value);
			}
			protected String getter(DotLayoutSettings object) {
				return getCommand();
			}
		});

		properties.add(new PropertyDeclaration<DotLayoutSettings, Boolean>(
				this, "Import connections shape from Dot graph", Boolean.class, true, false, false) {
			protected void setter(DotLayoutSettings object, Boolean value) {
				setImportConnectionsShape(value);
			}
			protected Boolean getter(DotLayoutSettings object) {
				return getImportConnectionsShape();
			}
		});
	}

	@Override
	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void load(Config config) {
		setCommand(config.getString(keyDotCommand, defaultDotCommand));
		setImportConnectionsShape(config.getBoolean(keyImportConnectionsShape, defaultImportConnectionsShape));
	}

	@Override
	public void save(Config config) {
		config.set(keyDotCommand, getCommand());
		config.setBoolean(keyImportConnectionsShape, getImportConnectionsShape());
	}

	@Override
	public String getSection() {
		return "Layout";
	}

	@Override
	public String getName() {
		return "Dot";
	}

	public static String getCommand() {
		return dotCommand;
	}

	public static void setCommand(String value) {
		dotCommand = value;
	}

	public static Boolean getImportConnectionsShape() {
		return importConnectionsShape;
	}

	public static void setImportConnectionsShape(Boolean value) {
		importConnectionsShape = value;
	}
}
