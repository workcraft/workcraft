package org.workcraft.gui.propertyeditor;

import java.util.List;

public interface PropertyEditable {
	public List<PropertyDeclaration> getPropertyDeclarations();
	public void addListener(PropertyEditableListener listener);
	public void removeListener(PropertyEditableListener listener);
	public void firePropertyChanged(String propertyName);
}
