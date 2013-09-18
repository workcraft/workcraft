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

public class PunfUtilitySettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;

	private static final String commandKey = "Tools.punf.command";
	private static final String extraArgsKey = "Tools.punf.args";

	private static String command = "punf";
	private static String extraArgs = "-r";

	public PunfUtilitySettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration(this, "Punf command", "getCommand", "setCommand", String.class));
		properties.add(new PropertyDeclaration(this, "Additional command line arguments", "getExtraArgs", "setExtrafArgs", String.class));
	}

	@Override
	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void load(Config config) {
		command = config.getString(commandKey, "punf");
		extraArgs = config.getString(extraArgsKey, "");
	}

	@Override
	public void save(Config config) {
		config.set(commandKey, command);
		config.set(extraArgsKey, extraArgs);
	}

	@Override
	public String getSection() {
		return "External tools";
	}

	@Override
	public String getName() {
		return "Punf";
	}

	public static String getCommand() {
		return command;
	}

	public static void setCommand(String value) {
		PunfUtilitySettings.command = value;
	}

	public static String getExtraArgs() {
		return extraArgs;
	}

	public static void setExtraArgs(String value) {
		PunfUtilitySettings.extraArgs = value;
	}

}