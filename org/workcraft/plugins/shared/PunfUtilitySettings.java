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

@DisplayName("punf")
public class PunfUtilitySettings implements PersistentPropertyEditable, Plugin {
	private static LinkedList<PropertyDescriptor> properties;

	private static String punfCommand = "punf";
	private static String punfArgs = "";

	private static final String punfCommandKey = "Verification.punf.command";
	private static final String punfArgsKey = "Verification.punf.args";

	public PunfUtilitySettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration(this, "Punf command", "getPunfCommand", "setPunfCommand", String.class));
		properties.add(new PropertyDeclaration(this, "Additional command line arguments", "getPunfArgs", "setPunfArgs", String.class));
	}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void loadPersistentProperties(Config config) {
		punfCommand = config.getString(punfCommandKey, "punf");
		punfArgs = config.getString(punfArgsKey, "");
	}

	public void storePersistentProperties(Config config) {
		config.set(punfCommandKey, punfCommand);
		config.set(punfArgsKey, punfArgs);
	}

	public String getSection() {
		return "Verification";
	}

	public static String getPunfCommand() {
		return punfCommand;
	}

	public static void setPunfCommand(String punfCommand) {
		PunfUtilitySettings.punfCommand = punfCommand;
	}

	public static String getPunfArgs() {
		return punfArgs;
	}

	public static void setPunfArgs(String punfArgs) {
		PunfUtilitySettings.punfArgs = punfArgs;
	}
}