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

package org.workcraft.plugins.mpsat;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class MpsatSynthesisUtilitySettings implements Settings {
	private static final LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
	private static final String prefix = "Tools.mpsatSynthesis";

	private static final String keyCommand = prefix + ".command";
	private static final String keyExtraArgs = prefix + ".args";

	private static final String defaultCommand = (DesktopApi.getOs().isWindows() ? "tools\\unfolding\\mpsat.exe" : "tools/unfolding/mpsat");
	private static final String defaultExtraArgs = "";

	private static String command = defaultCommand;
	private static String extraArgs = defaultExtraArgs;

	public MpsatSynthesisUtilitySettings() {
		properties.add(new PropertyDeclaration<MpsatSynthesisUtilitySettings, String>(
				this, "MPSat command for synthesis", String.class, true, false, false) {
			protected void setter(MpsatSynthesisUtilitySettings object, String value) {
				setCommand(value);
			}
			protected String getter(MpsatSynthesisUtilitySettings object) {
				return getCommand();
			}
		});

		properties.add(new PropertyDeclaration<MpsatSynthesisUtilitySettings, String>(
				this, "Additional parameters", String.class, true, false, false) {
			protected void setter(MpsatSynthesisUtilitySettings object, String value) {
				setExtraArgs(value);
			}
			protected String getter(MpsatSynthesisUtilitySettings object) {
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
		setCommand(config.getString(keyCommand, defaultCommand));
		setExtraArgs(config.getString(keyExtraArgs, defaultExtraArgs));
	}

	@Override
	public void save(Config config) {
		config.set(keyCommand, getCommand());
		config.set(keyExtraArgs, getExtraArgs());
	}

	@Override
	public String getSection() {
		return "External tools";
	}

	@Override
	public String getName() {
		return "MPSat synthesis";
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
