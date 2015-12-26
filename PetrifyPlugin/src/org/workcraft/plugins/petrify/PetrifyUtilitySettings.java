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

package org.workcraft.plugins.petrify;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class PetrifyUtilitySettings implements Settings {
	public static final String BUNDLED_DIRECTORY = "tools/petrify/";

	private static final LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
	private static final String prefix = "Tools";

	private static final String keyPetrifyCkeyPmmand = prefix + ".petrify.command";
	private static final String keyPetrifyArgs = prefix + ".petrify.args";
	private static final String keyUseBundledVersion = prefix + ".useBundledVersion";

	private static final String defaultPetrifyCommand = "petrify";
	private static final String defaultPetrifyArgs = "";
	private static Boolean defaultUseBundledVersion = true;

	private static String petrifyCommand = defaultPetrifyCommand;
	private static String petrifyArgs = defaultPetrifyArgs;
	private static Boolean useBundledVersion = defaultUseBundledVersion;

	public PetrifyUtilitySettings() {
		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "Petrify command", String.class, true, false, false) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				setPetrifyCommand(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return getPetrifyCommand();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "Additional parameters for Petrify", String.class, true, false, false) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				setPetrifyArgs(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return getPetrifyArgs();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, Boolean>(
				this, "Use bundled version (in " + BUNDLED_DIRECTORY + ")", Boolean.class, true, false, false) {
			protected void setter(PetrifyUtilitySettings object, Boolean value) {
				setUseBundledVersion(value);
			}
			protected Boolean getter(PetrifyUtilitySettings object) {
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
		setPetrifyCommand(config.getString(keyPetrifyCkeyPmmand, defaultPetrifyCommand));
		setPetrifyArgs(config.getString(keyPetrifyArgs, defaultPetrifyArgs));
		setUseBundledVersion(config.getBoolean(keyUseBundledVersion, defaultUseBundledVersion));
	}

	@Override
	public void save(Config config) {
		config.set(keyPetrifyCkeyPmmand, getPetrifyCommand());
		config.set(keyPetrifyArgs, getPetrifyArgs());
		config.setBoolean(keyUseBundledVersion, getUseBundledVersion());
	}

	@Override
	public String getSection() {
		return "External tools";
	}

	@Override
	public String getName() {
		return "Petrify";
	}

	public static String getPetrifyCommand() {
		return petrifyCommand;
	}

	public static void setPetrifyCommand(String value) {
		petrifyCommand = value;
	}

	public static String getPetrifyArgs() {
		return petrifyArgs;
	}

	public static void setPetrifyArgs(String value) {
		petrifyArgs = value;
	}

	public static Boolean getUseBundledVersion() {
		return useBundledVersion;
	}

	public static void setUseBundledVersion(Boolean value) {
		useBundledVersion = value;
	}

}
