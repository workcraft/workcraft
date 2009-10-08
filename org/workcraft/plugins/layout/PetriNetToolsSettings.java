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
import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.gui.propertyeditor.PersistentPropertyEditable;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;

@DisplayName ("Tool locations")
public class PetriNetToolsSettings implements PersistentPropertyEditable, Plugin {
	protected static String pcompCommand = "pcomp";
	protected static String punfCommand = "punf";
	protected static String mpsatCommand = "mpsat";
	protected static String petrifyCommand = "petrify";

	private static LinkedList<PropertyDescriptor> properties;

	public PetriNetToolsSettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration("PComp command", "getPcompCommand", "setPcompCommand", String.class));
		properties.add(new PropertyDeclaration("PUNF command", "getPunfCommand", "setPunfCommand", String.class));
		properties.add(new PropertyDeclaration("MPSat command", "getMpsatCommand", "setMpsatCommand", String.class));
		properties.add(new PropertyDeclaration("Petrify command", "getPetrifyCommand", "setPetrifyCommand", String.class));

	}
	public List<PropertyDescriptor> getPropertyDeclarations() {
		return properties;
	}

	public void loadPersistentProperties(Config config) {
		pcompCommand = config.getString("PetriTools.pcompCommand", "pcomp");
		punfCommand = config.getString("PetriTools.punfCommand", "punf");
		mpsatCommand = config.getString("PetriTools.mpsatCommand", "mpsat");
		petrifyCommand = config.getString("PetriTools.petrifyCommand", "petrify");
	}

	public void storePersistentProperties(Config config) {
		config.set("PetriTools.pcompCommand", pcompCommand);
		config.set("PetriTools.punfCommand", punfCommand);
		config.set("PetriTools.mpsatCommand", mpsatCommand);
		config.set("PetriTools.petrifyCommand", petrifyCommand);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
	}

	public void firePropertyChanged(String propertyName) {
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
	}

	public String getSection() {
		return "Petri Net Tools";
	}
	public static String getPcompCommand() {
		return pcompCommand;
	}
	public static void setPcompCommand(String pcompCommand) {
		PetriNetToolsSettings.pcompCommand = pcompCommand;
	}
	public static String getPunfCommand() {
		return punfCommand;
	}
	public static void setPunfCommand(String punfCommand) {
		PetriNetToolsSettings.punfCommand = punfCommand;
	}
	public static String getMpsatCommand() {
		return mpsatCommand;
	}
	public static void setMpsatCommand(String mpsatCommand) {
		PetriNetToolsSettings.mpsatCommand = mpsatCommand;
	}
	public static String getPetrifyCommand() {
		return petrifyCommand;
	}
	public static void setPetrifyCommand(String petrifyCommand) {
		PetriNetToolsSettings.petrifyCommand = petrifyCommand;
	}
}

