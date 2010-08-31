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

package org.workcraft.plugins.layout;

import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.Plugin;
import org.workcraft.annotations.DisplayName;
import org.workcraft.gui.propertyeditor.PersistentPropertyEditable;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;

@DisplayName ("Dot")
public class DotLayoutSettings implements PersistentPropertyEditable, Plugin {
	public static double dotPositionScaleFactor = 0.02;
	protected static String tmpGraphFilePath = "tmp.dot";
	protected static String dotCommand = "dot";

	private static LinkedList<PropertyDescriptor> properties;

	public DotLayoutSettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration(this, "Dot command", "getDotCommand", "setDotCommand", String.class));
		properties.add(new PropertyDeclaration(this, "Temporary dot file path", "getTmpGraphFilePath", "setTmpGraphFilePath", String.class));
		properties.add(new PropertyDeclaration(this, "Dot position scale factor", "getDotPositionScaleFactor", "setDotPositionScaleFactor", double.class));
	}
	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void loadPersistentProperties(Config config) {
		dotCommand = config.getString("DotLayout.dotCommand", "dot");
		tmpGraphFilePath = config.getString("DotLayout.tmpGraphFilePath", "tmp.dot");
		dotPositionScaleFactor = config.getDouble("DotLayout.dotPositionScaleFactor", 0.02);
	}

	public void storePersistentProperties(Config config) {
		config.set("DotLayout.dotCommand", dotCommand)	;
		config.set("DotLayout.tmpGraphFilePath", tmpGraphFilePath);
		config.set("DotLayout.dotPositionScaleFactor", Double.toString(dotPositionScaleFactor));
	}

	public static String getTmpGraphFilePath() {
		return DotLayoutSettings.tmpGraphFilePath;
	}

	public static void setTmpGraphFilePath(String tmpGraphFilePath) {
		DotLayoutSettings.tmpGraphFilePath = tmpGraphFilePath;
	}

	public static String getDotCommand() {
		return DotLayoutSettings.dotCommand;
	}

	public static void setDotCommand(String dotCommand) {
		DotLayoutSettings.dotCommand = dotCommand;
	}

	public static double getDotPositionScaleFactor() {
		return DotLayoutSettings.dotPositionScaleFactor;
	}

	public static void setDotPositionScaleFactor(double dotPositionScaleFactor) {
		DotLayoutSettings.dotPositionScaleFactor = dotPositionScaleFactor;
	}

	public String getSection() {
		return "Layout";
	}
}
