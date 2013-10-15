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

package org.workcraft.plugins.dfs;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class DfsSettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;
	private static Color computedLogicColor  = new Color (153, 153, 153);
	private static Color synchronisationRegisterColor = new Color (153, 153, 153);

	public DfsSettings() {
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration<DfsSettings, Color>(
				this, "Computed logic color", Color.class) {
			protected void setter(DfsSettings object, Color value) {
				DfsSettings.setComputedLogicColor(value);
			}
			protected Color getter(DfsSettings object) {
				return DfsSettings.getComputedLogicColor();
			}
		});

		properties.add(new PropertyDeclaration<DfsSettings, Color>(
				this, "Register synchronisation color", Color.class) {
			protected void setter(DfsSettings object, Color value) {
				DfsSettings.setSynchronisationRegisterColor(value);
			}
			protected Color getter(DfsSettings object) {
				return DfsSettings.getSynchronisationRegisterColor();
			}
		});
	}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void load(Config config) {
		setComputedLogicColor(config.getColor("DfsSettings.computedLogicColor", new Color (153, 153, 153)));
		setSynchronisationRegisterColor(config.getColor("DfsSettings.synchronisationRegisterColor", new Color (153, 153, 153)));
	}

	public void save(Config config) {
		config.setColor("DfsSettings.computedLogicColor", getComputedLogicColor());
		config.setColor("DfsSettings.synchronisationRegisterColor", getSynchronisationRegisterColor());
	}

	public String getSection() {
		return "Models";
	}

	public String getName() {
		return "Dataflow Structure";
	}

	public static Color getComputedLogicColor() {
		return computedLogicColor;
	}

	public static void setComputedLogicColor(Color value) {
		DfsSettings.computedLogicColor = value;
	}

	public static Color getSynchronisationRegisterColor() {
		return synchronisationRegisterColor;
	}

	public static void setSynchronisationRegisterColor(Color value) {
		DfsSettings.synchronisationRegisterColor = value;
	}
}
