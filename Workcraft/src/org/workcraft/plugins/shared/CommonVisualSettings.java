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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class CommonVisualSettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;

	protected static double size = 1.0;
	protected static double strokeWidth = 0.1;
	protected static int iconSize = 16;
	protected static Color backgroundColor = Color.WHITE;
	protected static Color foregroundColor = Color.BLACK;
	protected static Color fillColor = Color.WHITE;
	private static Positioning textPositioning = Positioning.TOP;

	public CommonVisualSettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration(this, "Base icon width (pixels, 8-256)",
				"getIconSize", "setIconSize", int.class));

		properties.add(new PropertyDeclaration(this, "Base component size (cm)",
				"getSize", "setSize", double.class));

		properties.add(new PropertyDeclaration(this, "Default stroke width (cm)",
				"getStrokeWidth", "setStrokeWidth", double.class));

		properties.add(new PropertyDeclaration(this, "Editor background color",
				"getBackgroundColor", "setBackgroundColor", Color.class));

		properties.add(new PropertyDeclaration(this, "Default foreground color",
				"getForegroundColor", "setForegroundColor", Color.class));

		properties.add(new PropertyDeclaration(this, "Default fill color",
				"getFillColor", "setFillColor", Color.class));

		LinkedHashMap<String, Object> positions = new LinkedHashMap<String, Object>();
		for(Positioning lp : Positioning.values())
			positions.put(lp.name, lp);
		properties.add(new PropertyDeclaration(this, "Default text positioning",
				"getTextPositioning", "setTextPositioning", Positioning.class, positions));
	}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void load(Config config) {
		size = config.getDouble("CommonVisualSettings.size", 1.0);
		iconSize = config.getInt("Commo`nVisualSettings.iconSize", 16);
		strokeWidth = config.getDouble("CommonVisualSettings.strokeWidth", 0.1);
		backgroundColor = config.getColor("CommonVisualSettings.backgroundColor", Color.WHITE);
		foregroundColor = config.getColor("CommonVisualSettings.foregroundColor", Color.BLACK);
		fillColor = config.getColor("CommonVisualSettings.fillColor", Color.WHITE);
		textPositioning = config.getTextPositioning("CommonVisualSettings.textPositioning", Positioning.TOP);
	}

	public void save(Config config) {
		config.setInt("CommonVisualSettings.iconSize", iconSize);
		config.setDouble("CommonVisualSettings.size", size);
		config.setDouble("CommonVisualSettings.strokeWidth", strokeWidth);
		config.setColor("CommonVisualSettings.backgroundColor", backgroundColor);
		config.setColor("CommonVisualSettings.foregroundColor", foregroundColor);
		config.setColor("CommonVisualSettings.fillColor", fillColor);
		config.setTextPositioning("CommonVisualSettings.textPositioning", getTextPositioning());
	}

	public static Color getBackgroundColor() {
		return backgroundColor;
	}

	public static void setBackgroundColor(Color backgroundColor) {
		CommonVisualSettings.backgroundColor = backgroundColor;
	}

	public static Color getForegroundColor() {
		return foregroundColor;
	}

	public static void setForegroundColor(Color foregroundColor) {
		CommonVisualSettings.foregroundColor = foregroundColor;
	}

	public static Color getFillColor() {
		return fillColor;
	}

	public static void setFillColor(Color fillColor) {
		CommonVisualSettings.fillColor = fillColor;
	}

	public static Positioning getTextPositioning() {
		return textPositioning;
	}

	public static void setTextPositioning(Positioning textPositioning) {
		CommonVisualSettings.textPositioning = textPositioning;
	}

	public String getSection() {
		return "Visual";
	}

	public static double getSize() {
		return size;
	}

	public static void setSize(double size) {
		CommonVisualSettings.size = size;

	}

	public static double getStrokeWidth() {
		return strokeWidth;
	}

	public static void setStrokeWidth(double strokeWidth) {
		CommonVisualSettings.strokeWidth = strokeWidth;
	}

	public static int getIconSize() {
		return iconSize;
	}

	public static void setIconSize(int iconSize) {
		if (iconSize<8)
			iconSize = 8;
		if (iconSize>256)
			iconSize = 256;
		CommonVisualSettings.iconSize = iconSize;
	}

	@Override
	public String getName() {
		return "Common visual settings";
	}
}
