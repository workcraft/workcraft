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

package org.workcraft.plugins.shared;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class PetrifyUtilitySettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;

	private static final String petrifyCommandKey = "Tools.petrify.command";
	private static final String petrifyArgsKey = "Tools.petrify.args";
	private static final String drawAstgCommandKey = "Tools.draw_astg.command";
	private static final String drawAstgArgsKey = "Tools.draw_astg.args";
	private static final String writeSgCommandKey = "Tools.write_sg.command";
	private static final String writeSgArgsKey = "Tools.write_sg.args";

	private static String petrifyCommand = "petrify";
	private static String petrifyArgs = "";
	private static String drawAstgCommand = "draw_astg";
	private static String drawAstgArgs = "";
	private static String writeSgCommand = "write_sg";
	private static String writeSgArgs = "";



	public PetrifyUtilitySettings() {
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "petrify command", String.class) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				PetrifyUtilitySettings.setPetrifyCommand(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return PetrifyUtilitySettings.getPetrifyCommand();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "Additional petrify command line arguments", String.class) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				PetrifyUtilitySettings.setPetrifyArgs(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return PetrifyUtilitySettings.getPetrifyArgs();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "write_sg command", String.class) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				PetrifyUtilitySettings.setWriteSgCommand(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return PetrifyUtilitySettings.getWriteSgCommand();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "Additional write_sg command line arguments", String.class) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				PetrifyUtilitySettings.setWriteSgArgs(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return PetrifyUtilitySettings.getWriteSgArgs();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "draw_astg command", String.class) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				PetrifyUtilitySettings.setDrawAstgCommand(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return PetrifyUtilitySettings.getDrawAstgCommand();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "Additional draw_astg command line arguments", String.class) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				PetrifyUtilitySettings.setDrawAstgArgs(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return PetrifyUtilitySettings.getDrawAstgArgs();
			}
		});
	}

	@Override
	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void load(Config config) {
		petrifyCommand = config.getString(petrifyCommandKey, "petrify");
		petrifyArgs = config.getString(petrifyArgsKey, "-nosi -lib tools/petrify.lib");
		drawAstgCommand = config.getString(drawAstgCommandKey, "draw_astg");
		drawAstgArgs= config.getString(drawAstgArgsKey, "");
		writeSgCommand = config.getString(writeSgCommandKey, "write_sg");
		writeSgArgs= config.getString(writeSgArgsKey, "");

	}

	@Override
	public void save(Config config) {
		config.set(petrifyCommandKey, petrifyCommand);
		config.set(petrifyArgsKey, petrifyArgs);
		config.set(drawAstgCommandKey, drawAstgCommand);
		config.set(drawAstgArgsKey, drawAstgArgs);
		config.set(writeSgCommandKey, writeSgCommand);
		config.set(writeSgArgsKey, writeSgArgs);
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

	public static void setPetrifyCommand(String petrifyCommand) {
		PetrifyUtilitySettings.petrifyCommand = petrifyCommand;
	}

	public static String getPetrifyArgs() {
		return petrifyArgs;
	}

	public static void setPetrifyArgs(String value) {
		PetrifyUtilitySettings.petrifyArgs = value;
	}

	public static String getDrawAstgCommand() {
		return drawAstgCommand;
	}

	public static void setDrawAstgCommand(String value) {
		PetrifyUtilitySettings.drawAstgCommand = value;
	}

	public static String getDrawAstgArgs() {
		return drawAstgArgs;
	}

	public static void setDrawAstgArgs(String value) {
		PetrifyUtilitySettings.drawAstgArgs = value;
	}

	public static String getWriteSgCommand() {
		return writeSgCommand;
	}

	public static void setWriteSgCommand(String value) {
		PetrifyUtilitySettings.writeSgCommand = value;
	}

	public static String getWriteSgArgs() {
		return writeSgArgs;
	}

	public static void setWriteSgArgs(String value) {
		PetrifyUtilitySettings.writeSgArgs = value;
	}

}
