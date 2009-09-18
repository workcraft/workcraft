package org.workcraft.serialisation.xml;

interface DeserialiserFactory {
	public XMLDeserialiser getDeserialiserFor(String className) throws InstantiationException, IllegalAccessException;
}
