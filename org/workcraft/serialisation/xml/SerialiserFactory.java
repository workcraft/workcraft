package org.workcraft.serialisation.xml;

public interface SerialiserFactory {
	public XMLSerialiser getSerialiserFor(Class<?> cls) throws InstantiationException, IllegalAccessException;
}