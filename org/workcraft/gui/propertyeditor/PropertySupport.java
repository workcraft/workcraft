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

package org.workcraft.gui.propertyeditor;

import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.dom.visual.PropertyChangeListener;

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
