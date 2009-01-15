package org.workcraft.gui.propertyeditor;

import java.util.List;

import org.workcraft.dom.visual.PropertyChangeListener;

public interface PropertyEditable {
	public List<PropertyDeclaration> getPropertyDeclarations();
	public void addListener(PropertyChangeListener listener);
	public void removeListener(PropertyChangeListener listener);
	public void firePropertyChanged(String propertyName);
}
