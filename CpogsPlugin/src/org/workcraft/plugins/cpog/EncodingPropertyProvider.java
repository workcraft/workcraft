package org.workcraft.plugins.cpog;

import org.workcraft.gui.propertyeditor.PropertyClass;
import org.workcraft.gui.propertyeditor.PropertyClassProvider;
import org.workcraft.gui.propertyeditor.cpog.EncodingProperty;

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
