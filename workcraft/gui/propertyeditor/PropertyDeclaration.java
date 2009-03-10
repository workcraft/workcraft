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

	@Override
	public Map<Object, String> getChoice() {
		return valueNames;
	}

	@Override
	public Object getValue(Object owner) {
		try {
			return owner.getClass().getMethod(getter).invoke(owner);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setValue(Object owner, Object value) {
		try {
			owner.getClass().getMethod(setter, cls).invoke(owner, value);
		} catch (Exception e) {
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
}