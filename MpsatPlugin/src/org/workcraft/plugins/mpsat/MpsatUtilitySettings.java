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
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.mpsat.MpsatSettings.SolutionMode;

public class MpsatUtilitySettings implements Settings {
	private static final LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
	private static final String prefix = "Tools.mpsat";

	private static final String keyCommand = prefix + ".command";
	private static final String keySolutionMode = prefix + ".solutionMode";
	private static final String keyExtraArgs = prefix + ".args";
	private static final String keyUsePnmlUnfolding = prefix + ".usePnmlUnfolding";
	private static final String keyDebugReach = prefix + ".debugReach";
	private static final String keyDebugTemporaryFiles = prefix + ".debugTemporaryFiles";

	private static final String defaultCommand = "mpsat";
	private static final SolutionMode defaultSolutionMode = SolutionMode.MINIMUM_COST;
	private static final String defaultExtraArgs = "";
	private static final Boolean defaultUsePnmlUnfolding = false;
	private static final Boolean defaultDebugReach = false;
	private static final Boolean defaultDebugTemporaryFiles = false;

	private static String command = defaultCommand;
	private static SolutionMode solutionMode = defaultSolutionMode;
	private static String extraArgs = defaultExtraArgs;
	private static Boolean usePnmlUnfolding = defaultUsePnmlUnfolding;
	private static Boolean debugReach = defaultDebugReach;
	private static Boolean debigTemporaryFiles = defaultDebugTemporaryFiles;

	public MpsatUtilitySettings() {
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
				this, "Use PNML-based unfolding (where possible)", Boolean.class) {
			protected void setter(MpsatUtilitySettings object, Boolean value) {
				MpsatUtilitySettings.setUsePnmlUnfolding(value);
			}
			protected Boolean getter(MpsatUtilitySettings object) {
				return MpsatUtilitySettings.getUsePnmlUnfolding();
			}
		});

		properties.add(new PropertyDeclaration<MpsatUtilitySettings, Boolean>(
				this, "Print out Reach expressions (debug)", Boolean.class) {
			protected void setter(MpsatUtilitySettings object, Boolean value) {
				MpsatUtilitySettings.setDebugReach(value);
			}
			protected Boolean getter(MpsatUtilitySettings object) {
				return MpsatUtilitySettings.getDebugReach();
			}
		});

		properties.add(new PropertyDeclaration<MpsatUtilitySettings, Boolean>(
				this, "Keep temporary files (debug)", Boolean.class) {
			protected void setter(MpsatUtilitySettings object, Boolean value) {
				MpsatUtilitySettings.setDebugTemporaryFiles(value);
			}
			protected Boolean getter(MpsatUtilitySettings object) {
				return MpsatUtilitySettings.getDebugTemporaryFiles();
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
		setSolutionMode(config.getEnum(keySolutionMode, SolutionMode.class, defaultSolutionMode));
		setExtraArgs(config.getString(keyExtraArgs, defaultExtraArgs));
		setUsePnmlUnfolding(config.getBoolean(keyUsePnmlUnfolding, defaultUsePnmlUnfolding));
		setDebugReach(config.getBoolean(keyDebugReach, defaultDebugReach));
		setDebugTemporaryFiles(config.getBoolean(keyDebugTemporaryFiles, defaultDebugTemporaryFiles));
	}

	@Override
	public void save(Config config) {
		config.set(keyCommand, getCommand());
		config.setEnum(keySolutionMode, SolutionMode.class, getSolutionMode());
		config.set(keyExtraArgs, getExtraArgs());
		config.setBoolean(keyUsePnmlUnfolding, getUsePnmlUnfolding());
		config.setBoolean(keyDebugReach, getDebugReach());
		config.setBoolean(keyDebugTemporaryFiles, getDebugTemporaryFiles());

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
		command = value;
	}

	public static String getExtraArgs() {
		return extraArgs;
	}

	public static void setExtraArgs(String value) {
		extraArgs = value;
	}

	public static void setSolutionMode(SolutionMode value) {
		solutionMode = value;
	}

	public static SolutionMode getSolutionMode() {
		return solutionMode;
	}

	public static int getSolutionCount() {
		return (solutionMode == SolutionMode.ALL) ? 10 : 1;
	}

	public static Boolean getUsePnmlUnfolding() {
		return usePnmlUnfolding;
	}

	public static void setUsePnmlUnfolding(Boolean value) {
		usePnmlUnfolding = value;
	}

	public static Boolean getDebugReach() {
		return debugReach;
	}

	public static void setDebugReach(Boolean value) {
		debugReach = value;
	}

	public static Boolean getDebugTemporaryFiles() {
		return debigTemporaryFiles;
	}

	public static void setDebugTemporaryFiles(Boolean value) {
		debigTemporaryFiles = value;
	}

	public static String getUnfoldingExtension() {
		return (getUsePnmlUnfolding() ? ".pnml" : ".mci");
	}

	public static String getCommandSuffix() {
		return (getUsePnmlUnfolding() ? "_pnml" : "");
	}

}
