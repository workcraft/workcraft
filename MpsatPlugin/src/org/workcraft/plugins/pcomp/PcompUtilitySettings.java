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

package org.workcraft.plugins.pcomp;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class PcompUtilitySettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;

	private static String pcompCommand = "pcomp";
	private static String pcompArgs = "";

	private static final String pcompCommandKey = "Tools.pcomp.command";
	private static final String pcompArgsKey = "Tools.pcomp.args";

	public PcompUtilitySettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration(this, "PComp command", "getPcompCommand", "setPcompCommand", String.class));
		properties.add(new PropertyDeclaration(this, "Additional command line arguments", "getPcompArgs", "setPcompArgs", String.class));
	}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void load(Config config) {
		pcompCommand = config.getString(pcompCommandKey, "pcomp");
		pcompArgs = config.getString(pcompArgsKey, "");
	}

	public void save(Config config) {
		config.set(pcompCommandKey, pcompCommand);
		config.set(pcompArgsKey, pcompArgs);
	}

	public String getSection() {
		return "External tools";
	}

	public static String getPcompCommand() {
		return pcompCommand;
	}

	public static void setPcompCommand(String pcompCommand) {
		PcompUtilitySettings.pcompCommand = pcompCommand;
	}

	public static String getPcompArgs() {
		return pcompArgs;
	}

	public static void setPcompArgs(String pcompArgs) {
		PcompUtilitySettings.pcompArgs = pcompArgs;
	}

	@Override
	public String getName() {
		return "PComp";
	}
}