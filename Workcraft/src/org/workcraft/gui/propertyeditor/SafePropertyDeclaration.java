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

public class SafePropertyDeclaration<O, V> implements PropertyDescriptor {
	private final O object;
	public String name;
	public Getter<O, V> getter;
	public Setter<O, V> setter;
	public Class<V> cls;
	public Map<String, V> predefinedValues;
	public Map<V, String> valueNames;
	private boolean choice;
	private boolean combinable;

	public boolean isChoice() {
		return choice;
	}

	public SafePropertyDeclaration(O object, String name, Getter<O, V> getter, Setter<O, V> setter, Class<V> cls) {
		this(object, name, getter, setter, cls, true);
	}

	public SafePropertyDeclaration(O object, String name, Getter<O, V> getter, Setter<O, V> setter, Class<V> cls, boolean combinable) {
		this(object, name, getter, setter, cls, null, combinable);
	}

	public SafePropertyDeclaration(O object, String name, Getter<O, V> getter, Setter<O, V> setter, Class<V> cls, Map<String, V> predefinedValues) {
		this(object, name, getter, setter, cls, predefinedValues, true);
	}

	public SafePropertyDeclaration(O object, String name, Getter<O, V> getter, Setter<O, V> setter, Class<V> cls, Map<String, V> predefinedValues, boolean combinable) {
		this.object = object;
		this.name = name;
		this.getter = getter;
		this.setter = setter;
		this.cls = cls;
		this.predefinedValues = predefinedValues;
		this.choice = (predefinedValues != null);
		if (choice) {
			valueNames = new LinkedHashMap<V, String>();
			for (String k : predefinedValues.keySet()) {
				valueNames.put(predefinedValues.get(k), k);
			}
		} else {
			valueNames = null;
		}
		this.combinable = combinable;
	}

	@Override
	public Map<? extends Object, String> getChoice() {
		return valueNames;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return getter.eval(object);
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		try {
			setter.eval(object, cls.cast(value));
		} catch (ClassCastException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<V> getType() {
		return cls;
	}

	@Override
	public boolean isWritable() {
		return setter != null;
	}

	@Override
	public boolean isCombinable() {
		return combinable;
	}
}