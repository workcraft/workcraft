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
import org.workcraft.Plugin;
import org.workcraft.annotations.DisplayName;
import org.workcraft.gui.propertyeditor.PersistentPropertyEditable;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;

@DisplayName("petrify")
public class PetrifyUtilitySettings implements PersistentPropertyEditable, Plugin {
	private static LinkedList<PropertyDescriptor> properties;

	private static String petrifyCommand = "petrify";
	private static String petrifyArgs = "";

	private static final String petrifyCommandKey = "Tools.petrify.command";
	private static final String petrifyArgsKey = "Tools.petrify.args";

	public PetrifyUtilitySettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration(this, "Petrify command", "getPetrifyCommand", "setPetrifyCommand", String.class));
		properties.add(new PropertyDeclaration(this, "Additional command line arguments", "getPetrifyArgs", "setPetrifyArgs", String.class));
	}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void loadPersistentProperties(Config config) {
		petrifyCommand = config.getString(petrifyCommandKey, "petrify");
		petrifyArgs = config.getString(petrifyArgsKey, "");
	}

	public void storePersistentProperties(Config config) {
		config.set(petrifyCommandKey, petrifyCommand);
		config.set(petrifyArgsKey, petrifyArgs);
	}

	public String getSection() {
		return "External tools";
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

	public static void setPetrifyArgs(String petrifyArgs) {
		PetrifyUtilitySettings.petrifyArgs = petrifyArgs;
	}
}