package org.workcraft.gui.propertyeditor;

import java.util.Collection;

import org.workcraft.dom.visual.PropertyChangeListener;

public interface PropertyEditable {
	public Collection<PropertyDescriptor> getPropertyDeclarations();
	public void addPropertyChangeListener(PropertyChangeListener listener);
	public void removePropertyChangeListener(PropertyChangeListener listener);
	public void firePropertyChanged(String propertyName);
}
