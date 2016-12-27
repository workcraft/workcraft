package org.workcraft.serialisation.xml;

interface DeserialiserFactory {
    XMLDeserialiser getDeserialiserFor(String className) throws InstantiationException, IllegalAccessException;
}
