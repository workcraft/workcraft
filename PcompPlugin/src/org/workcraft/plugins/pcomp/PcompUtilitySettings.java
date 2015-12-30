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

package org.workcraft.plugins.pcomp;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class PcompUtilitySettings implements Settings {

	private static final LinkedList<PropertyDescriptor> properties  = new LinkedList<PropertyDescriptor>();
	private static final String prefix = "Tools.pcomp";

	private static final String ketCommand = prefix + ".command";
	private static final String keyExtraArgs = prefix + ".args";

	private static final String defaultCommand = (DesktopApi.getOs().isWindows() ? "tools\\unfolding\\pcomp.exe" : "tools/unfolding/pcomp");
	private static final String defaultExtraArgs = "";

	private static String command = defaultCommand;
	private static String extraArgs = defaultExtraArgs;

	public PcompUtilitySettings() {
		properties.add(new PropertyDeclaration<PcompUtilitySettings, String>(
				this, "PComp command", String.class, true, false, false) {
			protected void setter(PcompUtilitySettings object, String value) {
				setCommand(value);
			}
			protected String getter(PcompUtilitySettings object) {
				return getCommand();
			}
		});

		properties.add(new PropertyDeclaration<PcompUtilitySettings, String>(
				this, "Additional parameters", String.class, true, false, false) {
			protected void setter(PcompUtilitySettings object, String value) {
				setExtraArgs(value);
			}
			protected String getter(PcompUtilitySettings object) {
				return getExtraArgs();
			}
		});
	}

	@Override
	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void load(Config config) {
		setCommand(config.getString(ketCommand, defaultCommand));
		setExtraArgs(config.getString(keyExtraArgs, defaultExtraArgs));
	}

	@Override
	public void save(Config config) {
		config.set(ketCommand, getCommand());
		config.set(keyExtraArgs, getExtraArgs());
	}

	@Override
	public String getSection() {
		return "External tools";
	}

	@Override
	public String getName() {
		return "PComp";
	}

	public static String getCommand() {
		return command;
	}

	public static void setCommand(String value) {
		command = value;
	}

	public static String getExtraArgs() {
		return extraArgs;
	}

	public static void setExtraArgs(String value) {
		extraArgs = value;
	}

}
