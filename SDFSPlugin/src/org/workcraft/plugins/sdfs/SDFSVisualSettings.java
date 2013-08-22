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
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class SDFSVisualSettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;
	private static Color computedLogicColor  = new Color (153, 153, 255);
	private static Color enabledRegisterColor = new Color (153, 255, 153);

	public SDFSVisualSettings() {
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration(this, "Computed logic color",
				"getComputedLogicColor", "setComputedLogicColor", Color.class));

		properties.add(new PropertyDeclaration(this, "Enabled register color",
				"getEnabledRegisterColor", "setEnabledRegisterColor", Color.class));
}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void load(Config config) {
		setComputedLogicColor(config.getColor("SDFS.VisualSettings.computedLogicColor", new Color (153, 153, 255)));
		setEnabledRegisterColor(config.getColor("SDFS.VisualSettings.enabledRegisterColor", new Color (153, 255, 153)));
	}

	public void save(Config config) {
		config.setColor("SDFS.VisualSettings.computedLogicColor", getComputedLogicColor());
		config.setColor("SDFS.VisualSettings.enabledRegisterColor", getEnabledRegisterColor());
	}

	public String getSection() {
		return "Visual";
	}

	public String getName() {
		return "SDFS";
	}

	public static Color getComputedLogicColor() {
		return computedLogicColor;
	}

	public static void setComputedLogicColor(Color computedLogicColor) {
		SDFSVisualSettings.computedLogicColor = computedLogicColor;
	}

	public static Color getEnabledRegisterColor() {
		return enabledRegisterColor;
	}

	public static void setEnabledRegisterColor(Color enabledRegisterColor) {
		SDFSVisualSettings.enabledRegisterColor = enabledRegisterColor;
	}
}