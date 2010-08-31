package org.workcraft.gui.propertyeditor;


public interface PropertyClassProvider {
	Class<?> getPropertyType();
	PropertyClass getPropertyGui();
}
