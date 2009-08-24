package org.workcraft.plugins.petri;

import java.util.LinkedList;
import java.util.List;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.framework.Config;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.gui.propertyeditor.PersistentPropertyEditable;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;

@DisplayName("Petri Net")
public class PetriNetSettings implements PersistentPropertyEditable, Plugin {
	private static LinkedList<PropertyDescriptor> properties;


	public PetriNetSettings() {
		properties = new LinkedList<PropertyDescriptor>();
	}

	public List<PropertyDescriptor> getPropertyDeclarations() {
		return properties;
	}

	public void loadPersistentProperties(Config config) {
	}

	public void storePersistentProperties(Config config) {
	}

	public String getSection() {
		return "Visual";
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
	}


	public void firePropertyChanged(String propertyName) {
	}


	public void removePropertyChangeListener(PropertyChangeListener listener) {
	}

}
