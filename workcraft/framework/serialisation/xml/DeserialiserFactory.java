package org.workcraft.framework.serialisation.xml;

interface DeserialiserFactory {
	public XMLDeserialiser getDeserialiserFor(String className) throws InstantiationException, IllegalAccessException;
}
