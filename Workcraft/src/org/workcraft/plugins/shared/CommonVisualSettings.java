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

	protected static double baseSize = 1.0;
	protected static double strokeWidth = 0.1;
	protected static Color borderColor = Color.BLACK;
	protected static Color fillColor = Color.WHITE;
	private static Positioning textPositioning = Positioning.TOP;
	private static boolean useEnabledForegroundColor = true;
	private static Color enabledForegroundColor = new Color(1.0f, 0.5f, 0.0f);
	private static boolean useEnabledBackgroundColor = false;
	private static Color enabledBackgroundColor = new Color(0.0f, 0.0f, 0.0f);

	public String getSection() {
		return "Common";
	}

	public String getName() {
		return "Visual";
	}

	public CommonVisualSettings() {
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration(this, "Base size (cm)",
				"getBaseSize", "setBaseSize", double.class));

		properties.add(new PropertyDeclaration(this, "Stroke width (cm)",
				"getStrokeWidth", "setStrokeWidth", double.class));

		properties.add(new PropertyDeclaration(this, "Border color",
				"getBorderColor", "setBorderColor", Color.class));

		properties.add(new PropertyDeclaration(this, "Fill color",
				"getFillColor", "setFillColor", Color.class));

		LinkedHashMap<String, Object> positions = new LinkedHashMap<String, Object>();
		for(Positioning lp : Positioning.values()) {
			positions.put(lp.name, lp);
		}
		properties.add(new PropertyDeclaration(this, "Default text positioning",
				"getTextPositioning", "setTextPositioning", Positioning.class, positions));

		properties.add(new PropertyDeclaration(this, "Use enabled component foreground",
				"getUseEnabledForegroundColor", "setUseEnabledForegroundColor", Boolean.class));

		properties.add(new PropertyDeclaration(this, "Enabled component foreground",
				"getEnabledForegroundColor", "setEnabledForegroundColor", Color.class));

		properties.add(new PropertyDeclaration(this, "Use enabled component background",
				"getUseEnabledBackgroundColor", "setUseEnabledBackgroundColor", Boolean.class));

		properties.add(new PropertyDeclaration(this, "Enabled component background",
				"getEnabledBackgroundColor", "setEnabledBackgroundColor", Color.class));
	}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void load(Config config) {
		baseSize = config.getDouble("CommonVisualSettings.baseSize", 1.0);
		strokeWidth = config.getDouble("CommonVisualSettings.strokeWidth", 0.1);
		borderColor = config.getColor("CommonVisualSettings.foregroundColor", Color.BLACK);
		fillColor = config.getColor("CommonVisualSettings.fillColor", Color.WHITE);
		textPositioning = config.getTextPositioning("CommonVisualSettings.textPositioning", Positioning.TOP);

		useEnabledForegroundColor = config.getBoolean("CommonVisualSettings.useEnabledForegroundColor", true);
		enabledForegroundColor = config.getColor("CommonVisualSettings.enabledForegroundColor", new Color(1.0f, 0.5f, 0.0f));

		useEnabledBackgroundColor = config.getBoolean("CommonVisualSettings.useEnabledBackgroundColor", false);
		enabledBackgroundColor = config.getColor("CommonVisualSettings.enabledBackgroundColor", new Color(1.0f, 0.5f, 0.0f));
	}

	public void save(Config config) {
		config.setDouble("CommonVisualSettings.baseSize", baseSize);
		config.setDouble("CommonVisualSettings.strokeWidth", strokeWidth);
		config.setColor("CommonVisualSettings.borderColor", borderColor);
		config.setColor("CommonVisualSettings.fillColor", fillColor);
		config.setTextPositioning("CommonVisualSettings.textPositioning", getTextPositioning());
		config.setBoolean("CommonVisualSettings.useEnabledForegroundColor", useEnabledForegroundColor);
		config.setColor("CommonVisualSettings.enabledBackgroundColor", enabledBackgroundColor);
		config.setBoolean("CommonVisualSettings.useEnabledBackgroundColor", useEnabledBackgroundColor);
		config.setColor("CommonVisualSettings.enabledForegroundColor", enabledForegroundColor);
	}

	public static double getBaseSize() {
		return baseSize;
	}

	public static void setBaseSize(double value) {
		CommonVisualSettings.baseSize = value;
	}

	public static double getStrokeWidth() {
		return strokeWidth;
	}

	public static void setStrokeWidth(double value) {
		CommonVisualSettings.strokeWidth = value;
	}

	public static Color getBorderColor() {
		return borderColor;
	}

	public static void setBorderColor(Color value) {
		CommonVisualSettings.borderColor = value;
	}

	public static Color getFillColor() {
		return fillColor;
	}

	public static void setFillColor(Color value) {
		CommonVisualSettings.fillColor = value;
	}

	public static Positioning getTextPositioning() {
		return textPositioning;
	}

	public static void setTextPositioning(Positioning value) {
		CommonVisualSettings.textPositioning = value;
	}

	public static void setUseEnabledForegroundColor(Boolean value) {
		CommonVisualSettings.useEnabledForegroundColor = value;
	}

	public static Boolean getUseEnabledForegroundColor() {
		return useEnabledForegroundColor;
	}

	public static void setEnabledForegroundColor(Color value) {
		CommonVisualSettings.enabledForegroundColor = value;
	}

	public static Color getEnabledForegroundColor() {
		return useEnabledForegroundColor ? enabledForegroundColor : null;
	}

	public static void setUseEnabledBackgroundColor(Boolean value) {
		CommonVisualSettings.useEnabledBackgroundColor = value;
	}

	public static Boolean getUseEnabledBackgroundColor() {
		return useEnabledBackgroundColor;
	}

	public static void setEnabledBackgroundColor(Color value) {
		CommonVisualSettings.enabledBackgroundColor = value;
	}

	public static Color getEnabledBackgroundColor() {
		return useEnabledBackgroundColor ? enabledBackgroundColor : null;
	}

}
