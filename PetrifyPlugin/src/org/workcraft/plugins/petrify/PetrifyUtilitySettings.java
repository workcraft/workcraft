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
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class PetrifyUtilitySettings implements Settings {

	private static final LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
	private static final String prefix = "Tools.petrify";

	private static final String keyPetrifyCkeyPmmand = prefix + ".command";
	private static final String keyPetrifyArgs = prefix + ".args";
	private static final String keyAdvancedMode= prefix + ".advancedMode";
	private static final String keyPrintStdout= prefix + ".printStdout";
	private static final String keyPrintStderr= prefix + ".printStderr";

	private static final String defaultCommand = (DesktopApi.getOs().isWindows() ? "tools\\PetrifyTools\\petrify.exe" : "tools/PetrifyTools/petrify");
	private static final String defaultArgs = "";
	private static final Boolean defaultAdvancedMode = false;
	private static final Boolean defaultPrintStdout = true;
	private static final Boolean defaultPrintStderr = true;

	private static String command = defaultCommand;
	private static String args = defaultArgs;
	private static Boolean advancedMode = defaultAdvancedMode;
	private static Boolean printStdout = defaultPrintStdout;
	private static Boolean printStderr = defaultPrintStderr;

	public PetrifyUtilitySettings() {
		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "Petrify command", String.class, true, false, false) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				setCommand(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return getCommand();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "Additional parameters", String.class, true, false, false) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				setArgs(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return getArgs();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, Boolean>(
				this, "Edit additional parameters before every call", Boolean.class, true, false, false) {
			protected void setter(PetrifyUtilitySettings object, Boolean value) {
				setAdvancedMode(value);
			}
			protected Boolean getter(PetrifyUtilitySettings object) {
				return getAdvancedMode();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, Boolean>(
				this, "Output stdout", Boolean.class, true, false, false) {
			protected void setter(PetrifyUtilitySettings object, Boolean value) {
				setPrintStdout(value);
			}
			protected Boolean getter(PetrifyUtilitySettings object) {
				return getPrintStdout();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, Boolean>(
				this, "Output stderr", Boolean.class, true, false, false) {
			protected void setter(PetrifyUtilitySettings object, Boolean value) {
				setPrintStderr(value);
			}
			protected Boolean getter(PetrifyUtilitySettings object) {
				return getPrintStderr();
			}
		});
	}

	@Override
	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void load(Config config) {
		setCommand(config.getString(keyPetrifyCkeyPmmand, defaultCommand));
		setArgs(config.getString(keyPetrifyArgs, defaultArgs));
		setAdvancedMode(config.getBoolean(keyAdvancedMode, defaultAdvancedMode));
		setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
		setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
	}

	@Override
	public void save(Config config) {
		config.set(keyPetrifyCkeyPmmand, getCommand());
		config.set(keyPetrifyArgs, getArgs());
		config.setBoolean(keyAdvancedMode, getAdvancedMode());
		config.setBoolean(keyPrintStdout, getPrintStdout());
		config.setBoolean(keyPrintStderr, getPrintStderr());
	}

	@Override
	public String getSection() {
		return "External tools";
	}

	@Override
	public String getName() {
		return "Petrify";
	}

	public static String getCommand() {
		return command;
	}

	public static void setCommand(String value) {
		command = value;
	}

	public static String getArgs() {
		return args;
	}

	public static void setArgs(String value) {
		args = value;
	}

	public static Boolean getAdvancedMode() {
		return advancedMode;
	}

	public static void setAdvancedMode(Boolean value) {
		advancedMode = value;
	}

	public static Boolean getPrintStdout() {
		return printStdout;
	}

	public static void setPrintStdout(Boolean value) {
		printStdout = value;
	}

	public static Boolean getPrintStderr() {
		return printStderr;
	}

	public static void setPrintStderr(Boolean value) {
		printStderr = value;
	}

}
