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
	private final VisualBreezeComponent me;

	public BreezePropertyDescriptor(java.beans.PropertyDescriptor propertyDescriptor, VisualBreezeComponent me) {
		this.propertyDescriptor = propertyDescriptor;
		this.me = me;
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

	public Object getValue() {
		try {
			return propertyDescriptor.getReadMethod().invoke(me.balsaComponent);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isWritable() {
		return propertyDescriptor.getWriteMethod() != null;
	}

	public void setValue(Object value) {
		try {
			propertyDescriptor.getWriteMethod().invoke(me.balsaComponent, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
