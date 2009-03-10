package org.workcraft.gui.propertyeditor;

import java.util.Map;

public interface PropertyDescriptor {
	public boolean isWritable();
	public Object getValue(Object owner);
	public void setValue(Object owner, Object value);
	public Map<Object, String> getChoice();
	public String getName();
	public Class<?> getType();
}
