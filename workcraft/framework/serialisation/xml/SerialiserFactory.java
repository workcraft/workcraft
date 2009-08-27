package org.workcraft.framework.serialisation.xml;

public interface SerialiserFactory {
	public XMLSerialiser getSerialiserFor(Class<?> cls) throws InstantiationException, IllegalAccessException;
}