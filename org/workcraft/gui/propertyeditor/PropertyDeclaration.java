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

import java.util.HashMap;
import java.util.Map;

public class PropertyDeclaration implements PropertyDescriptor {
	public String name;
	public String getter;
	public String setter;

	public Class<?> cls;
	public Map<String, Object> predefinedValues;
	public Map<Object, String> valueNames;

	private boolean choice;

	public boolean isChoice() {
		return choice;
	}

	public PropertyDeclaration (String name, String getter, String setter, Class<?> cls ) {
		this.name = name;
		this.getter = getter;
		this.setter = setter;
		this.cls = cls;
		this.predefinedValues = null;
		this.valueNames = null;

		choice = false;
	}

	public PropertyDeclaration (String name, String getter, String setter, Class<?> cls, Map<String, Object> predefinedValues) {
		this.name = name;
		this.getter = getter;
		this.setter = setter;
		this.cls = cls;
		this.predefinedValues = predefinedValues;

		valueNames = new HashMap<Object, String>();

		for (String k : predefinedValues.keySet()) {
			valueNames.put(predefinedValues.get(k), k);
		}

		choice = true;
	}

	public Map<Object, String> getChoice() {
		return valueNames;
	}

	public Object getValue(Object owner) {
		try {
			return owner.getClass().getMethod(getter).invoke(owner);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setValue(Object owner, Object value) {
		try {
			owner.getClass().getMethod(setter, cls).invoke(owner, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return cls;
	}

	public boolean isWritable() {
		return setter != null;
	}
}