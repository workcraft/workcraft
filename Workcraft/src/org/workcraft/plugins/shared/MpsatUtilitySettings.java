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
import org.workcraft.LegacyPlugin;
import org.workcraft.annotations.DisplayName;
import org.workcraft.gui.propertyeditor.PersistentPropertyEditable;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;

@DisplayName("MPSat")
public class MpsatUtilitySettings implements PersistentPropertyEditable, LegacyPlugin {
	private static LinkedList<PropertyDescriptor> properties;

	private static String mpsatCommand = "mpsat";
	private static String mpsatArgs = "";

	private static final String mpsatCommandKey = "Tools.mpsat.command";
	private static final String mpsatArgsKey = "Tools.mpsat.args";

	public MpsatUtilitySettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration(this, "MPSat command", "getMpsatCommand", "setMpsatCommand", String.class));
		properties.add(new PropertyDeclaration(this, "MPSat additional arguments", "getMpsatArgs", "setMpsatArgs", String.class));
	}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void loadPersistentProperties(Config config) {
		mpsatCommand = config.getString(mpsatCommandKey, "mpsat");
		mpsatArgs = config.getString(mpsatArgsKey, "");
	}

	public void storePersistentProperties(Config config) {
		config.set(mpsatCommandKey, mpsatCommand);
		config.set(mpsatArgsKey, mpsatArgs);
	}

	public String getSection() {
		return "External tools";
	}

	public static String getMpsatCommand() {
		return mpsatCommand;
	}

	public static void setMpsatCommand(String mpsatCommand) {
		MpsatUtilitySettings.mpsatCommand = mpsatCommand;
	}

	public static String getMpsatArgs() {
		return mpsatArgs;
	}

	public static void setMpsatArgs(String mpsatArgs) {
		MpsatUtilitySettings.mpsatArgs = mpsatArgs;
	}

}