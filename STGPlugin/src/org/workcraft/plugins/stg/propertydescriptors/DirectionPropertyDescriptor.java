package org.workcraft.plugins.stg.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;

public class DirectionPropertyDescriptor implements PropertyDescriptor {
	private final STG stg;
	private final Node st;

	public DirectionPropertyDescriptor(STG stg, Node st) {
		this.stg = stg;
		this.st = st;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return stg.getDirection(st);
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		stg.setDirectionWithAutoInstance(st, (Direction)value);
	}

	@Override
	public Map<Object, String> getChoice() {
		LinkedHashMap<Object, String> directions = new LinkedHashMap<Object, String>();
		directions.put(SignalTransition.Direction.PLUS, "+");
		directions.put(SignalTransition.Direction.MINUS, "-");
		directions.put(SignalTransition.Direction.TOGGLE, "~");
		return directions;
	}

	@Override
	public String getName() {
		return "Direction";
	}

	@Override
	public Class<?> getType() {
		return int.class;
	}
}
