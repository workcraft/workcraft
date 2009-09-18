package org.workcraft.plugins.layout;

import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.Plugin;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.gui.propertyeditor.PersistentPropertyEditable;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;

@DisplayName ("Dot")
public class PetriNetToolsSettings implements PersistentPropertyEditable, Plugin {
	protected static String pcompCommand = "pcomp";
	protected static String punfCommand = "punf";
	protected static String mpsatCommand = "mpsat";
	protected static String dummyRenameCommand = "dummyRename";

	private static LinkedList<PropertyDescriptor> properties;

	public PetriNetToolsSettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration("PComp command", "getPcompCommand", "setPcompCommand", String.class));
		properties.add(new PropertyDeclaration("PUNF command", "getPunfCommand", "setPunfCommand", String.class));
		properties.add(new PropertyDeclaration("MPSat command", "getMpsatCommand", "setMpsatCommand", String.class));
		properties.add(new PropertyDeclaration("Dummy rename command", "getDummyRenameCommand", "setDummyRenameCommand", String.class));

	}
	public List<PropertyDescriptor> getPropertyDeclarations() {
		return properties;
	}

	public void loadPersistentProperties(Config config) {
		pcompCommand = config.getString("PetriTools.pcompCommand", "pcomp");
		punfCommand = config.getString("PetriTools.punfCommand", "punf");
		mpsatCommand = config.getString("PetriTools.mpsatCommand", "mpsat");
		dummyRenameCommand = config.getString("PetriTools.dummyRenameCommand", "dummyRename");
	}

	public void storePersistentProperties(Config config) {
		config.set("PetriTools.pcompCommand", pcompCommand);
		config.set("PetriTools.punfCommand", punfCommand);
		config.set("PetriTools.mpsatCommand", mpsatCommand);
		config.set("PetriTools.dummyRenameCommand", dummyRenameCommand);
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
	public static String getDummyRenameCommand() {
		return dummyRenameCommand;
	}
	public static void setDummyRenameCommand(String dummyRenameCommand) {
		PetriNetToolsSettings.dummyRenameCommand = dummyRenameCommand;
	}
}

