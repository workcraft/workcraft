package org.workcraft.plugins.fsm.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.FormatException;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.Symbol;

public class SymbolPropertyDescriptor implements PropertyDescriptor {
	private final Fsm fsm;
	private final Symbol symbol;

	public SymbolPropertyDescriptor(Fsm fsm, Symbol symbol) {
		this.fsm = fsm;
		this.symbol = symbol;
	}

	@Override
	public Map<Object, String> getChoice() {
		return null;
	}

	@Override
	public String getName() {
		return fsm.getName(symbol) + " name";
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
		return fsm.getName(symbol);
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		String name = (String)value;
		Node node = fsm.getNodeByReference(name);
    	if (node == null) {
    		fsm.setName(symbol, name);
    		for (Event event: fsm.getEvents(symbol)) {
    			event.sendNotification(new PropertyChangedEvent(event, Event.PROPERTY_SYMBOL));
    		}
    	} else if (node instanceof Symbol) {
    	} else {
    		throw new FormatException("Node \"" + name + "\" already exists and it is not a symbol.");
    	}
	}

}
