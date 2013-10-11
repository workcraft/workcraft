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

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class PropertyDeclaration implements PropertyDescriptor {
	public String name;
	public String getter;
	public String setter;
	public Class<?> cls;
	public Map<String, Object> predefinedValues;
	public Map<Object, String> valueNames;
	private boolean choice;
	private final Object object;

	public boolean isChoice() {
		return choice;
	}

	public PropertyDeclaration(Object object, String name, String getter, String setter, Class<?> cls) {
		this.object = object;
		this.name = name;
		this.getter = getter;
		this.setter = setter;
		this.cls = cls;
		this.predefinedValues = null;
		this.valueNames = null;
		this.choice = false;
	}

	public PropertyDeclaration(Object object, String name, String getter, String setter, Class<?> cls, LinkedHashMap<String, Object> predefinedValues) {
		this.object = object;
		this.name = name;
		this.getter = getter;
		this.setter = setter;
		this.cls = cls;
		this.predefinedValues = predefinedValues;
		this.valueNames = new LinkedHashMap<Object, String>();
		for (String k : predefinedValues.keySet()) {
			this.valueNames.put(predefinedValues.get(k), k);
		}
		this.choice = true;
	}

	@Override
	public Map<Object, String> getChoice() {
		return valueNames;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		try {
			return object.getClass().getMethod(getter).invoke(object);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		try {
			object.getClass().getMethod(setter, cls).invoke(object, value);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getType() {
		return cls;
	}

	@Override
	public boolean isWritable() {
		return setter != null;
	}

	@Override
	public boolean isCombinable() {
		return true;
	}

}