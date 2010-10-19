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

package org.workcraft.plugins.petri;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;

public class PetriNetSettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;

	private static Color enabledBackgroundColor = new Color(0.0f, 0.0f, 0.0f);
	private static Color enabledForegroundColor = new Color(1.0f, 0.5f, 0.0f);

	private static boolean useEnabledBackgroundColor = false;
	private static boolean useEnabledForegroundColor = true;

	public PetriNetSettings() {
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration(this, "Use enabled transition foreground", "getUseEnabledForegroundColor", "setUseEnabledForegroundColor", Boolean.class));
		properties.add(new PropertyDeclaration(this, "Enabled transition foreground", "getEnabledForegroundColor", "setEnabledForegroundColor", Color.class));

		properties.add(new PropertyDeclaration(this, "Use enabled transition background", "getUseEnabledBackgroundColor", "setUseEnabledBackgroundColor", Boolean.class));
		properties.add(new PropertyDeclaration(this, "Enabled transition background", "getEnabledBackgroundColor", "setEnabledBackgroundColor", Color.class));
	}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void load(Config config) {
		useEnabledForegroundColor = config.getBoolean("PetriNetSettings.useEnabledForegroundColor", true);
		enabledForegroundColor = config.getColor("PetriNetSettings.enabledForegroundColor", new Color(1.0f, 0.5f, 0.0f));

		useEnabledBackgroundColor = config.getBoolean("PetriNetSettings.useEnabledBackgroundColor", false);
		enabledBackgroundColor = config.getColor("PetriNetSettings.enabledBackgroundColor", new Color(1.0f, 0.5f, 0.0f));
	}

	public void save(Config config) {
		config.setBoolean("PetriNetSettings.useEnabledForegroundColor", useEnabledForegroundColor);
		config.setColor("PetriNetSettings.enabledBackgroundColor", enabledBackgroundColor);

		config.setBoolean("PetriNetSettings.useEnabledBackgroundColor", useEnabledBackgroundColor);
		config.setColor("PetriNetSettings.enabledForegroundColor", enabledForegroundColor);
	}

	public String getSection() {
		return "Visual";
	}

	@Override
	public String getName() {
		return "Petri Net";
	}

	public static void setEnabledBackgroundColor(Color enabledBackgroundColor) {
		PetriNetSettings.enabledBackgroundColor = enabledBackgroundColor;
	}

	public static Color getEnabledBackgroundColor() {
		return useEnabledBackgroundColor ? enabledBackgroundColor : null;
	}

	public static void setEnabledForegroundColor(Color enabledForegroundColor) {
		PetriNetSettings.enabledForegroundColor = enabledForegroundColor;
	}

	public static Color getEnabledForegroundColor() {
		return useEnabledForegroundColor ? enabledForegroundColor : null;
	}

	public static void setUseEnabledBackgroundColor(
			Boolean useEnabledBackgroundColor) {
		PetriNetSettings.useEnabledBackgroundColor = useEnabledBackgroundColor;
	}

	public static Boolean getUseEnabledBackgroundColor() {
		return useEnabledBackgroundColor;
	}

	public static void setUseEnabledForegroundColor(
			Boolean useEnabledForegroundColor) {
		PetriNetSettings.useEnabledForegroundColor = useEnabledForegroundColor;
	}

	public static Boolean getUseEnabledForegroundColor() {
		return useEnabledForegroundColor;
	}
}
