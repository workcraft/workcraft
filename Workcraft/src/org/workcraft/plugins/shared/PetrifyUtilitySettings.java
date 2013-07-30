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

	private static String petrifyCommand = "petrify";
	private static String petrifyArgs = "";

	private static final String petrifyCommandKey = "Tools.petrify.command";
	private static final String petrifyArgsKey = "Tools.petrify.args";

	private static String draw_astgCommand = "draw_astg";
	private static String draw_astgArgs = "";

	private static final String draw_astgCommandKey = "Tools.draw_astg.command";
	private static final String draw_astgArgsKey = "Tools.draw_astg.args";

	private static String write_sgCommand = "write_sg";
	private static String write_sgArgs = "";

	private static final String write_sgCommandKey = "Tools.write_sg.command";
	private static final String write_sgArgsKey = "Tools.write_sg.args";



	public PetrifyUtilitySettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration(this, "petrify command", "getPetrifyCommand", "setPetrifyCommand", String.class));
		properties.add(new PropertyDeclaration(this, "Additional petrify command line arguments", "getPetrifyArgs", "setPetrifyArgs", String.class));
		properties.add(new PropertyDeclaration(this, "write_sg command", "getWrite_sgCommand", "setWrite_sgCommand", String.class));
		properties.add(new PropertyDeclaration(this, "Additional write_sg command line arguments", "getWrite_sgArgs", "setWrite_sgArgs", String.class));
		properties.add(new PropertyDeclaration(this, "draw_astg command", "getDraw_astgCommand", "setDraw_astgCommand", String.class));
		properties.add(new PropertyDeclaration(this, "Additional draw_astg command line arguments", "getDraw_astgArgs", "setDraw_astgArgs", String.class));
	}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void load(Config config) {
		petrifyCommand = config.getString(petrifyCommandKey, "petrify");
		petrifyArgs = config.getString(petrifyArgsKey, "-nosi -lib tools/petrify.lib");
		draw_astgCommand = config.getString(draw_astgCommandKey, "draw_astg");
		draw_astgArgs= config.getString(draw_astgArgsKey, "");
		write_sgCommand = config.getString(write_sgCommandKey, "write_sg");
		write_sgArgs= config.getString(write_sgArgsKey, "");

	}

	public void save(Config config) {
		config.set(petrifyCommandKey, petrifyCommand);
		config.set(petrifyArgsKey, petrifyArgs);
		config.set(draw_astgCommandKey, draw_astgCommand);
		config.set(draw_astgArgsKey, draw_astgArgs);
		config.set(write_sgCommandKey, write_sgCommand);
		config.set(write_sgArgsKey, write_sgArgs);
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

	public static String getDraw_astgCommand() {
		return draw_astgCommand;
	}

	public static void setDraw_astgCommand(String draw_astgCommand) {
		PetrifyUtilitySettings.draw_astgCommand = draw_astgCommand;
	}

	public static String getDraw_astgArgs() {
		return draw_astgArgs;
	}

	public static void setDraw_astgArgs(String draw_astgArgs) {
		PetrifyUtilitySettings.draw_astgArgs = draw_astgArgs;
	}

	public static String getWrite_sgCommand() {
		return write_sgCommand;
	}

	public static void setWrite_sgCommand(String write_sgCommand) {
		PetrifyUtilitySettings.write_sgCommand = write_sgCommand;
	}

	public static String getWrite_sgArgs() {
		return write_sgArgs;
	}

	public static void setWrite_sgArgs(String write_sgArgs) {
		PetrifyUtilitySettings.write_sgArgs = write_sgArgs;
	}

	@Override
	public String getName() {
		return "Petrify";
	}
}