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

package org.workcraft.plugins.sdfs;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.Plugin;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.gui.propertyeditor.PersistentPropertyEditable;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.shared.CommonVisualSettings;

@DisplayName("SDFS")
public class SDFSVisualSettings implements PersistentPropertyEditable, Plugin {
	private static LinkedList<PropertyDescriptor> properties;

	private static boolean useGlobal = true;

	private static double size = 1.0;
	private static double strokeWidth = 0.1;

	private static Color backgroundColor = Color.WHITE;
	private static Color foregroundColor = Color.BLACK;
	private static Color fillColor = Color.WHITE;
	private static Color enabledRegisterColor = new Color (153, 255, 153);
	private static Color disabledRegisterColor = Color.WHITE;
	private static Color tokenColor = Color.BLACK;

	public SDFSVisualSettings() {
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration("Enabled register color", "getEnabledRegisterColor", "setEnabledRegisterColor", Color.class));
		properties.add(new PropertyDeclaration("Disabled register color", "getDisabledRegisterColor", "setDisabledRegisterColor", Color.class));
		properties.add(new PropertyDeclaration("Token color", "getTokenColor", "setTokenColor", Color.class));

		properties.add(new PropertyDeclaration ("Use global settings", "getUseGlobal", "setUseGlobal", boolean.class));
		properties.add(new PropertyDeclaration("Base component size (cm)", "getSize", "setSize", double.class));
		properties.add(new PropertyDeclaration("Default stroke width (cm)", "getStrokeWidth", "setStrokeWidth", double.class));

		properties.add(new PropertyDeclaration("Editor background color", "getBackgroundColor", "setBackgroundColor", Color.class));
		properties.add(new PropertyDeclaration("Default foreground color", "getForegroundColor", "setForegroundColor", Color.class));
		properties.add(new PropertyDeclaration("Default fill color", "getFillColor", "setFillColor", Color.class));
	}

	public List<PropertyDescriptor> getPropertyDeclarations() {
		return properties;
	}

	public void loadPersistentProperties(Config config) {
		useGlobal = config.getBoolean("SDFS.VisualSettings.useGlobal", true);
		size = config.getDouble("SDFS.VisualSettings.size", 1.0);
		strokeWidth = config.getDouble("SDFS.VisualSettings.strokeWidth", 0.1);
		backgroundColor = config.getColor("SDFS.VisualSettings.backgroundColor", Color.WHITE);
		foregroundColor = config.getColor("SDFS.VisualSettings.foregroundColor", Color.BLACK);
		fillColor = config.getColor("SDFS.VisualSettings.fillColor", Color.WHITE);
		enabledRegisterColor = config.getColor("SDFS.VisualSettings.enabledRegisterColor", new Color (153, 255, 153));
		disabledRegisterColor = config.getColor("SDFS.VisualSettings.disabledRegisterColor", Color.WHITE);
		tokenColor = config.getColor("SDFS.VisualSettings.tokenColor", Color.BLACK);
	}

	public void storePersistentProperties(Config config) {
		config.setBoolean("SDFS.VisualSettings.useGlobal", useGlobal);
		config.setDouble("SDFS.VisualSettings.size", size);
		config.setDouble("SDFS.VisualSettings.strokeWidth", strokeWidth);
		config.setColor("SDFS.VisualSettings.backgroundColor", backgroundColor);
		config.setColor("SDFS.VisualSettings.foregroundColor", foregroundColor);
		config.setColor("SDFS.VisualSettings.fillColor", fillColor);
		config.setColor("SDFS.VisualSettings.disabledRegisterColor", disabledRegisterColor);
		config.setColor("SDFS.VisualSettings.enabledRegisterColor", enabledRegisterColor);
		config.setColor("SDFS.VisualSettings.tokenColor", tokenColor);
	}

	public static Color getBackgroundColor() {
		if (useGlobal)
			return CommonVisualSettings.getBackgroundColor();
		else
			return CommonVisualSettings.getBackgroundColor();

	}

	public void setBackgroundColor(Color backgroundColor) {
		if (useGlobal)
			CommonVisualSettings.setBackgroundColor(backgroundColor);
		else
			SDFSVisualSettings.backgroundColor = backgroundColor;
	}

	public static Color getForegroundColor() {
		if (useGlobal)
			return CommonVisualSettings.getForegroundColor();
		else
			return foregroundColor;
	}

	public void setForegroundColor(Color foregroundColor) {
		if (useGlobal)
			CommonVisualSettings.setForegroundColor(foregroundColor);
		else
			SDFSVisualSettings.foregroundColor = foregroundColor;
	}

	public static Color getFillColor() {
		if (useGlobal)
			return CommonVisualSettings.getFillColor();
		else
			return fillColor;
	}

	public void setFillColor(Color fillColor) {
		if (useGlobal)
			CommonVisualSettings.setFillColor(fillColor);
		else
			SDFSVisualSettings.fillColor = fillColor;
	}

	public String getSection() {
		return "Visual";
	}

	public static double getSize() {
		if (useGlobal)
			return CommonVisualSettings.getSize();
		else
			return size;
	}

	public static void setSize(double size) {
		if (useGlobal)
			CommonVisualSettings.setSize(size);
		else
			SDFSVisualSettings.size = size;

	}

	public static double getStrokeWidth() {
		if (useGlobal)
			return CommonVisualSettings.getStrokeWidth();
		else
			return strokeWidth;
	}

	public static void setStrokeWidth(double strokeWidth) {
		if (useGlobal)
			CommonVisualSettings.setStrokeWidth(strokeWidth);
		else
			SDFSVisualSettings.strokeWidth = strokeWidth;
	}



	public void addPropertyChangeListener(PropertyChangeListener listener) {
	}


	public void firePropertyChanged(String propertyName) {
	}


	public void removePropertyChangeListener(PropertyChangeListener listener) {
	}


	public static boolean getUseGlobal() {
		return useGlobal;
	}

	public static void setUseGlobal(boolean useGlobal) {
		SDFSVisualSettings.useGlobal = useGlobal;
	}

	public static Color getEnabledRegisterColor() {
		return enabledRegisterColor;
	}

	public static void setEnabledRegisterColor(Color enabledRegisterColor) {
		SDFSVisualSettings.enabledRegisterColor = enabledRegisterColor;
	}

	public static Color getDisabledRegisterColor() {
		return disabledRegisterColor;
	}

	public static void setDisabledRegisterColor(Color disabledRegisterColor) {
		SDFSVisualSettings.disabledRegisterColor = disabledRegisterColor;
	}

	public static Color getTokenColor() {
		return tokenColor;
	}

	public static void setTokenColor(Color tokenColor) {
		SDFSVisualSettings.tokenColor = tokenColor;
	}

}
