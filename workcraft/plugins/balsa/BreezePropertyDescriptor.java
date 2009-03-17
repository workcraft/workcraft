package org.workcraft.plugins.balsa;

import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;

public class BreezePropertyDescriptor implements PropertyDescriptor {

	private final java.beans.PropertyDescriptor propertyDescriptor;

	public BreezePropertyDescriptor(java.beans.PropertyDescriptor propertyDescriptor) {
		this.propertyDescriptor = propertyDescriptor;
	}

	public Map<Object, String> getChoice() {
		return null;
	}

	public String getName() {
		return propertyDescriptor.getDisplayName();
	}

	public Class<?> getType() {
		return propertyDescriptor.getPropertyType();
	}

	public Object getValue(Object owner) {
		try {
			VisualBreezeComponent component = (VisualBreezeComponent)owner;
			return propertyDescriptor.getReadMethod().invoke(component.balsaComponent);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isWritable() {
		return propertyDescriptor.getWriteMethod() != null;
	}

	public void setValue(Object owner, Object value) {
		try {
			VisualBreezeComponent component = (VisualBreezeComponent)owner;
			propertyDescriptor.getWriteMethod().invoke(component.balsaComponent, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
