package org.workcraft.plugins.son.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.son.connections.SONConnection;

public class ConnectionTimePropertyDescriptor implements PropertyDescriptor{
	private final SONConnection con;

	public ConnectionTimePropertyDescriptor(SONConnection con) {
		this.con = con;
	}

	@Override
	public String getName() {
		return "Time interval";
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public boolean isCombinable() {
		return false;
	}

	@Override
	public boolean isTemplatable() {
		return false;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return con.getTime();
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		con.setTime((String)value);

	}

	@Override
	public Map<? extends Object, String> getChoice() {
		return null;
	}

}
