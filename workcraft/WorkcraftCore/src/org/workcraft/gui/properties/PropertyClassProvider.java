package org.workcraft.gui.properties;

public interface PropertyClassProvider {
    Class<?> getPropertyType();
    PropertyClass<?, ?> getPropertyGui();
}
