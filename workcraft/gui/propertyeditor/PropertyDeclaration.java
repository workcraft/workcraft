package org.workcraft.gui.propertyeditor;

import java.util.HashMap;

public class PropertyDeclaration {
	public String name;
	public String getter;
	public String setter;

	public Class<?> cls;
	HashMap<String, Object> predefinedValues;


	public PropertyDeclaration (String name, String getter, String setter, Class<?> cls ) {
		this.name = name;
		this.getter = getter;
		this.setter = setter;
		this.cls = cls;
		this.predefinedValues = null;
	}

	public PropertyDeclaration (String name, String getter, String setter, Class<?> cls, HashMap<String, Object> predefinedValues) {
		this.name = name;
		this.getter = getter;
		this.setter = setter;
		this.cls = cls;
		this.predefinedValues = predefinedValues;
	}
}