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

	private static String punfCommand = "punf";
	private static String punfArgs = "";

	private static boolean punfRAComplexityReduction = false;

	private static final String punfCommandKey = "Tools.punf.command";
	private static final String punfArgsKey = "Tools.punf.args";

	private static final String punfRAComplexityReductionKey = "Tools.punf.RAComplexityReduction";

	public PunfUtilitySettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration(this, "Punf command", "getPunfCommand", "setPunfCommand", String.class));
		properties.add(new PropertyDeclaration(this, "Additional command line arguments", "getPunfArgs", "setPunfArgs", String.class));
//		properties.add(new PropertyDeclaration(this, "Do read-arc complexity reduction", "getDoRAComplexityReduction", "setDoRAComplexityReduction", boolean.class));
	}

	public static boolean getDoRAComplexityReduction() {
		return punfRAComplexityReduction;
	}

	public static void setDoRAComplexityReduction(boolean doReduction) {
		PunfUtilitySettings.punfRAComplexityReduction = doReduction;
	}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void load(Config config) {
		punfCommand = config.getString(punfCommandKey, "punf");
		punfArgs = config.getString(punfArgsKey, "");
		punfRAComplexityReduction = config.getBoolean(punfRAComplexityReductionKey, false);
	}

	public void save(Config config) {
		config.set(punfCommandKey, punfCommand);
		config.set(punfArgsKey, punfArgs);
		config.setBoolean(punfRAComplexityReductionKey, punfRAComplexityReduction );
	}

	public String getSection() {
		return "External tools";
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

	@Override
	public String getName() {
		return "punf";
	}
}