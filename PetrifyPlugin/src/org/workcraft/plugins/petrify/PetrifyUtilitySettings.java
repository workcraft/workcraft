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
	private static final LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
	private static final String prefix = "Tools";

	private static final String keyPetrifyCkeyPmmand = prefix + ".petrify.command";
	private static final String keyPetrifyArgs = prefix + ".petrify.args";
	private static final String keyDrawAstgCommand = prefix + ".draw_astg.command";
	private static final String keyDrawAstgArgs = prefix + ".draw_astg.args";
	private static final String keyWriteSgCommand = prefix + ".write_sg.command";
	private static final String keyWriteSgArgs = prefix + ".write_sg.args";

	private static final String defaultPetrifyCommand = "petrify";
	private static final String defaultPetrifyArgs = "";
	private static final String defaultDrawAstgCommand = "draw_astg";
	private static final String defaultDrawAstgArgs = "";
	private static final String defaultWriteSgCommand = "write_sg";
	private static final String defaultWriteSgArgs = "";

	private static String petrifyCommand = defaultPetrifyCommand;
	private static String petrifyArgs = defaultPetrifyArgs;
	private static String drawAstgCommand = defaultDrawAstgCommand;
	private static String drawAstgArgs = defaultDrawAstgArgs;
	private static String writeSgCommand = defaultWriteSgCommand;
	private static String writeSgArgs = defaultWriteSgArgs;

	public PetrifyUtilitySettings() {
		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "petrify command", String.class, true, false, false) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				setPetrifyCommand(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return getPetrifyCommand();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "Additional petrify command line arguments", String.class, true, false, false) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				setPetrifyArgs(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return getPetrifyArgs();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "write_sg command", String.class, true, false, false) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				setWriteSgCommand(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return getWriteSgCommand();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "Additional write_sg command line arguments", String.class, true, false, false) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				setWriteSgArgs(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return getWriteSgArgs();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "draw_astg command", String.class, true, false, false) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				setDrawAstgCommand(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return getDrawAstgCommand();
			}
		});

		properties.add(new PropertyDeclaration<PetrifyUtilitySettings, String>(
				this, "Additional draw_astg command line arguments", String.class, true, false, false) {
			protected void setter(PetrifyUtilitySettings object, String value) {
				setDrawAstgArgs(value);
			}
			protected String getter(PetrifyUtilitySettings object) {
				return getDrawAstgArgs();
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
		setDrawAstgCommand(config.getString(keyDrawAstgCommand, defaultDrawAstgCommand));
		setDrawAstgArgs(config.getString(keyDrawAstgArgs, defaultDrawAstgArgs));
		setWriteSgCommand(config.getString(keyWriteSgCommand, defaultWriteSgCommand));
		setWriteSgArgs(config.getString(keyWriteSgArgs, defaultWriteSgArgs));
	}

	@Override
	public void save(Config config) {
		config.set(keyPetrifyCkeyPmmand, getPetrifyCommand());
		config.set(keyPetrifyArgs, getPetrifyArgs());
		config.set(keyDrawAstgCommand, getDrawAstgCommand());
		config.set(keyDrawAstgArgs, getDrawAstgArgs());
		config.set(keyWriteSgCommand, getWriteSgCommand());
		config.set(keyWriteSgArgs, getWriteSgArgs());
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

	public static String getDrawAstgCommand() {
		return drawAstgCommand;
	}

	public static void setDrawAstgCommand(String value) {
		drawAstgCommand = value;
	}

	public static String getDrawAstgArgs() {
		return drawAstgArgs;
	}

	public static void setDrawAstgArgs(String value) {
		drawAstgArgs = value;
	}

	public static String getWriteSgCommand() {
		return writeSgCommand;
	}

	public static void setWriteSgCommand(String value) {
		writeSgCommand = value;
	}

	public static String getWriteSgArgs() {
		return writeSgArgs;
	}

	public static void setWriteSgArgs(String value) {
		writeSgArgs = value;
	}

}
