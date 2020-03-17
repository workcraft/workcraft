package org.workcraft.plugins.cpog;

import org.workcraft.gui.properties.PropertyClass;
import org.workcraft.gui.properties.PropertyClassProvider;
import org.workcraft.plugins.cpog.properties.EncodingProperty;

public class EncodingPropertyProvider implements PropertyClassProvider {

    @Override
    public Class<?> getPropertyType() {
        return Encoding.class;
    }

    @Override
    public PropertyClass getPropertyGui() {
        return new EncodingProperty();
    }

}
