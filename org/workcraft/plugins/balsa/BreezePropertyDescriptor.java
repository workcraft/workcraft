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

package org.workcraft.plugins.balsa;

import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;

public class BreezePropertyDescriptor implements PropertyDescriptor {

	private final java.beans.PropertyDescriptor propertyDescriptor;

	public BreezePropertyDescriptor(java.beans.PropertyDescriptor propertyDescriptor) {
		this.propertyDescriptor = propertyDescriptor;
	}

	public Map<Object, String> getChoice() {
		return null;
	}

	public String getName() {
		return propertyDescriptor.getDisplayName();
	}

	public Class<?> getType() {
		return propertyDescriptor.getPropertyType();
	}

	public Object getValue(Object owner) {
		try {
			VisualBreezeComponent component = (VisualBreezeComponent)owner;
			return propertyDescriptor.getReadMethod().invoke(component.balsaComponent);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isWritable() {
		return propertyDescriptor.getWriteMethod() != null;
	}

	public void setValue(Object owner, Object value) {
		try {
			VisualBreezeComponent component = (VisualBreezeComponent)owner;
			propertyDescriptor.getWriteMethod().invoke(component.balsaComponent, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
