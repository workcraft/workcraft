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
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.plugins.mpsat.MpsatSettings.SolutionMode;

public class MpsatUtilitySettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;

	private static final String commandKey = "Tools.mpsat.command";
	private static final String solutionModeKey = "Tools.mpsat.solutionMode";
	private static final String extraArgsKey = "Tools.mpsat.args";
	private static final String debugReachKey = "Tools.mpsat.debugReach";

	private static String command = "mpsat";
	private static SolutionMode solutionMode = SolutionMode.MINIMUM_COST;
	private static String extraArgs = "";
	private static Boolean debugReach = false;

	public MpsatUtilitySettings() {
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration<MpsatUtilitySettings, String>(
				this, "MPSat command", String.class) {
			protected void setter(MpsatUtilitySettings object, String value) {
				MpsatUtilitySettings.setCommand(value);
			}
			protected String getter(MpsatUtilitySettings object) {
				return MpsatUtilitySettings.getCommand();
			}
		});

		properties.add(new PropertyDeclaration<MpsatUtilitySettings, SolutionMode>(
				this, "Check mode", SolutionMode.class, SolutionMode.getChoice()) {
			protected void setter(MpsatUtilitySettings object, SolutionMode value) {
				MpsatUtilitySettings.setSolutionMode(value);
			}
			protected SolutionMode getter(MpsatUtilitySettings object) {
				return MpsatUtilitySettings.getSolutionMode();
			}
		});

		properties.add(new PropertyDeclaration<MpsatUtilitySettings, String>(
				this, "Additional arguments", String.class) {
			protected void setter(MpsatUtilitySettings object, String value) {
				MpsatUtilitySettings.setExtraArgs(value);
			}
			protected String getter(MpsatUtilitySettings object) {
				return MpsatUtilitySettings.getExtraArgs();
			}
		});

		properties.add(new PropertyDeclaration<MpsatUtilitySettings, Boolean>(
				this, "Debug Reach expressions", Boolean.class) {
			protected void setter(MpsatUtilitySettings object, Boolean value) {
				MpsatUtilitySettings.setDebugReach(value);
			}
			protected Boolean getter(MpsatUtilitySettings object) {
				return MpsatUtilitySettings.getDebugReach();
			}
		});
	}

	@Override
	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void load(Config config) {
		command = config.getString(commandKey, "mpsat");
		solutionMode = config.getEnum(solutionModeKey, SolutionMode.class, SolutionMode.FIRST);
		extraArgs = config.getString(extraArgsKey, "");
		debugReach = config.getBoolean(debugReachKey, false);
	}

	@Override
	public void save(Config config) {
		config.set(commandKey, command);
		config.setEnum(solutionModeKey, SolutionMode.class, solutionMode);
		config.set(extraArgsKey, extraArgs);
		config.setBoolean(debugReachKey, debugReach);
	}

	@Override
	public String getSection() {
		return "External tools";
	}

	@Override
	public String getName() {
		return "MPSat";
	}

	public static String getCommand() {
		return command;
	}

	public static void setCommand(String value) {
		MpsatUtilitySettings.command = value;
	}

	public static String getExtraArgs() {
		return extraArgs;
	}

	public static void setExtraArgs(String value) {
		MpsatUtilitySettings.extraArgs = value;
	}

	public static void setSolutionMode(SolutionMode value) {
		MpsatUtilitySettings.solutionMode = value;
	}

	public static SolutionMode getSolutionMode() {
		return solutionMode;
	}

	public static int getSolutionCount() {
		return (MpsatUtilitySettings.solutionMode == SolutionMode.ALL) ? 10 : 1;
	}

	public static Boolean getDebugReach() {
		return debugReach;
	}

	public static void setDebugReach(Boolean debug) {
		MpsatUtilitySettings.debugReach = debug;
	}
}