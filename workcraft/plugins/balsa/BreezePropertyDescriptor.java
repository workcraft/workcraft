package org.workcraft.plugins.balsa;

import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;

public class BreezePropertyDescriptor implements PropertyDescriptor {

	private final java.beans.PropertyDescriptor propertyDescriptor;

	public BreezePropertyDescriptor(java.beans.PropertyDescriptor propertyDescriptor) {
		this.propertyDescriptor = propertyDescriptor;
	}

	@Override
	public Map<Object, String> getChoice() {
		return null;
	}

	@Override
	public String getName() {
		return propertyDescriptor.getDisplayName();
	}

	@Override
	public Class<?> getType() {
		return propertyDescriptor.getPropertyType();
	}

	@Override
	public Object getValue(Object owner) {
		try {
			VisualBreezeComponent component = (VisualBreezeComponent)owner;
			return propertyDescriptor.getReadMethod().invoke(component.balsaComponent);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isWritable() {
		return propertyDescriptor.getWriteMethod() != null;
	}

	@Override
	public void setValue(Object owner, Object value) {
		try {
			VisualBreezeComponent component = (VisualBreezeComponent)owner;
			propertyDescriptor.getWriteMethod().invoke(component.balsaComponent, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
