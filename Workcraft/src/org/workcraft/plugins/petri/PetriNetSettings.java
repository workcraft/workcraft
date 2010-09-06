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

import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.LegacyPlugin;
import org.workcraft.annotations.DisplayName;
import org.workcraft.gui.propertyeditor.PersistentPropertyEditable;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;

@DisplayName("Petri Net")
public class PetriNetSettings implements PersistentPropertyEditable, LegacyPlugin {
	private static LinkedList<PropertyDescriptor> properties;


	public PetriNetSettings() {
		properties = new LinkedList<PropertyDescriptor>();
	}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void loadPersistentProperties(Config config) {
	}

	public void storePersistentProperties(Config config) {
	}

	public String getSection() {
		return "Visual";
	}
}
