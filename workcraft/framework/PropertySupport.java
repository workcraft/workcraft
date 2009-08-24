package org.workcraft.framework;

import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.PropertyEditable;

public class PropertySupport {
	private LinkedList<PropertyDescriptor> propertyDeclarations = new LinkedList<PropertyDescriptor>();
	private LinkedList<PropertyChangeListener> propertyChangeListeners = new LinkedList<PropertyChangeListener>();

	public PropertySupport() {

	}

	public PropertySupport(PropertyEditable inheritFrom) {
		for (PropertyDescriptor d : inheritFrom.getPropertyDeclarations())
			propertyDeclarations.add(d);
	}

	public Collection<PropertyDescriptor> getPropertyDeclarations() {
		return propertyDeclarations;
	}

	public void addPropertyDeclaration(PropertyDescriptor declaration) {
		propertyDeclarations.add(declaration);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeListeners.remove(listener);
	}

	public void firePropertyChanged(String propertyName, Object sender) {
		for (PropertyChangeListener l : propertyChangeListeners)
			l.onPropertyChanged(propertyName, sender);
	}
}
