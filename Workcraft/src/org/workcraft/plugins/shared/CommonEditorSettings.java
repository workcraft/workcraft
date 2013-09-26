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
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class CommonEditorSettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;

	protected static Color backgroundColor = Color.WHITE;
	private static boolean showGrid = true;
	private static boolean showRulers = true;
	protected static int iconSize = 24;

	public String getSection() {
		return "Common";
	}

	public String getName() {
		return "Editor";
	}

	public CommonEditorSettings() {
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration(this, "Background color",
				"getBackgroundColor", "setBackgroundColor", Color.class));

		properties.add(new PropertyDeclaration(this, "Show grid",
				"getShowGrid", "setShowGrid", Boolean.class));

		properties.add(new PropertyDeclaration(this, "Show rulers",
				"getShowRulers", "setShowRulers", Boolean.class));

		properties.add(new PropertyDeclaration(this, "Icon width (pixels, 8-256)",
				"getIconSize", "setIconSize", int.class));
	}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void load(Config config) {
		backgroundColor = config.getColor("CommonEditorSettings.backgroundColor", Color.WHITE);
		showGrid = config.getBoolean("CommonEditorSettings.showGrid", true);
		showRulers = config.getBoolean("CommonEditorSettings.showRullers", true);
		iconSize = config.getInt("CommonEditorSettings.iconSize", 24);
	}

	public void save(Config config) {
		config.setColor("CommonEditorSettings.backgroundColor", backgroundColor);
		config.setBoolean("CommonEditorSettings.showGrid", showGrid);
		config.setBoolean("CommonEditorSettings.showRullers", showRulers);
		config.setInt("CommonEditorSettings.iconSize", iconSize);
	}

	public static Color getBackgroundColor() {
		return backgroundColor;
	}

	public static void setBackgroundColor(Color value) {
		CommonEditorSettings.backgroundColor = value;
	}

	public static void setShowGrid(Boolean value) {
		CommonEditorSettings.showGrid = value;
	}

	public static Boolean getShowGrid() {
		return showGrid;
	}

	public static void setShowRulers(Boolean value) {
		CommonEditorSettings.showRulers = value;
	}

	public static Boolean getShowRulers() {
		return showRulers;
	}

	public static int getIconSize() {
		return iconSize;
	}

	public static void setIconSize(int value) {
		if (value < 8) {
			value = 8;
		}
		if (value > 256) {
			value = 256;
		}
		CommonEditorSettings.iconSize = value;
	}

}
