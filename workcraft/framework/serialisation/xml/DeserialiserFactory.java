package org.workcraft.framework.serialisation.xml;

public interface DeserialiserFactory {
	public XMLDeserialiser getDeserialiserFor(String className) throws InstantiationException, IllegalAccessException;
}
