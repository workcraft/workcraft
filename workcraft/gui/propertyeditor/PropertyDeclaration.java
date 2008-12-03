package org.workcraft.gui.propertyeditor;

public class PropertyDeclaration {
	public String name;
	public String getter;
	public String setter;
	public Class<?> cls;

	public PropertyDeclaration (String name, String getter, String setter, Class<?> cls ) {
		this.name = name;
		this.getter = getter;
		this.setter = setter;
		this.cls = cls;
	}
}