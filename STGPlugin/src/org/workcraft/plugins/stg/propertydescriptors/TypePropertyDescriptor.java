package org.workcraft.plugins.stg.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Type;

public class TypePropertyDescriptor implements PropertyDescriptor  {
	private final STG stg;
	private final Node st;

	public TypePropertyDescriptor(STG stg, Node st) {
		this.stg = stg;
		this.st = st;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return ((SignalTransition)st).getSignalType();
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		((SignalTransition)st).setSignalType((Type)value);
	}

	@Override
	public Map<Object, String> getChoice() {
		LinkedHashMap<Object, String> types = new LinkedHashMap<Object, String>();
		types.put(SignalTransition.Type.INPUT, "Input");
		types.put(SignalTransition.Type.OUTPUT, "Output");
		types.put(SignalTransition.Type.INTERNAL, "Internal");
		return types;
	}

	@Override
	public String getName() {
		return "Signal type";
	}

	@Override
	public Class<?> getType() {
		return int.class;
	}

	@Override
	public boolean isCombinable() {
		return true;
	}

}
