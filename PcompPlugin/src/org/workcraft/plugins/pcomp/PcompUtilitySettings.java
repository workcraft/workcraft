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
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class PcompUtilitySettings implements Settings {
	public static final String BUNDLED_DIRECTORY = "tools/pcomp/";

	private static final LinkedList<PropertyDescriptor> properties  = new LinkedList<PropertyDescriptor>();
	private static final String prefix = "Tools.pcomp";

	private static final String ketCommand = prefix + ".command";
	private static final String keyExtraArgs = prefix + ".args";
	private static final String keyUseBundledVersion = prefix + ".useBundledVersion";

	private static final String defaultCommand = "pcomp";
	private static final String defaultExtraArgs = "";
	private static Boolean defaultUseBundledVersion = true;

	private static String command = defaultCommand;
	private static String extraArgs = defaultExtraArgs;
	private static Boolean useBundledVersion = defaultUseBundledVersion;

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

		properties.add(new PropertyDeclaration<PcompUtilitySettings, Boolean>(
				this, "Use bundled version (in " + BUNDLED_DIRECTORY + ")", Boolean.class, true, false, false) {
			protected void setter(PcompUtilitySettings object, Boolean value) {
				setUseBundledVersion(value);
			}
			protected Boolean getter(PcompUtilitySettings object) {
				return getUseBundledVersion();
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
		setUseBundledVersion(config.getBoolean(keyUseBundledVersion, defaultUseBundledVersion));
	}

	@Override
	public void save(Config config) {
		config.set(ketCommand, getCommand());
		config.set(keyExtraArgs, getExtraArgs());
		config.setBoolean(keyUseBundledVersion, getUseBundledVersion());
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

	public static Boolean getUseBundledVersion() {
		return useBundledVersion;
	}

	public static void setUseBundledVersion(Boolean value) {
		useBundledVersion = value;
	}

}
